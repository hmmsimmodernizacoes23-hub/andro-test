package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class AudioEngine {
    private val TAG = "AudioEngine"
    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isRunning = false
    private var audioThread: Thread? = null

    // Thread-safe collection for active playing voices
    private val activeTones = ConcurrentLinkedQueue<ToneInstance>()

    data class ToneInstance(
        val frequency: Double,
        val type: WaveType,
        val durationSamples: Int,
        var currentSample: Int = 0,
        val volume: Float = 0.2f,
        val pitchSlideSpeed: Double = 0.0 // Change frequency over time (good for kick drum drops!)
    )

    enum class WaveType {
        SINE, SQUARE, TRIANGLE, SAWTOOTH, NOISE
    }

    /**
     * Trigger a retro synthesized tone.
     */
    fun playTone(
        frequency: Double,
        type: WaveType,
        durationMs: Int,
        volume: Float = 0.15f,
        pitchSlideSpeed: Double = 0.0
    ) {
        if (!isRunning) return
        val durationSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        activeTones.add(
            ToneInstance(
                frequency = frequency,
                type = type,
                durationSamples = durationSamples,
                currentSample = 0,
                volume = volume,
                pitchSlideSpeed = pitchSlideSpeed
            )
        )
    }

    /**
     * Start the real-time background synthesizer thread.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        activeTones.clear()

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(1024)

        try {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack: ${e.message}")
            isRunning = false
            return
        }

        audioThread = Thread {
            val shortBuffer = ShortArray(256)
            while (isRunning) {
                var activeCount = 0
                for (i in shortBuffer.indices) {
                    var sampleSum = 0.0
                    val iterator = activeTones.iterator()
                    while (iterator.hasNext()) {
                        val tone = iterator.next()
                        if (tone.currentSample >= tone.durationSamples) {
                            activeTones.remove(tone)
                            continue
                        }

                        activeCount++
                        // Slide pitch if set
                        val currentFreq = tone.frequency + (tone.pitchSlideSpeed * tone.currentSample / sampleRate)
                        val t = tone.currentSample.toDouble() / sampleRate

                        val sampleValue = when (tone.type) {
                            WaveType.SINE -> sin(2.0 * Math.PI * currentFreq * t)
                            WaveType.SQUARE -> if (sin(2.0 * Math.PI * currentFreq * t) > 0.0) 1.0 else -1.0
                            WaveType.TRIANGLE -> {
                                val period = 1.0 / currentFreq
                                val phase = (t % period) / period
                                4.0 * abs(phase - 0.5) - 1.0
                            }
                            WaveType.SAWTOOTH -> {
                                val period = 1.0 / currentFreq
                                val phase = (t % period) / period
                                2.0 * phase - 1.0
                            }
                            WaveType.NOISE -> Random.nextDouble(-1.0, 1.0)
                        }

                        // Apply envelope (simple smooth linear decay)
                        val decayFactor = ((tone.durationSamples - tone.currentSample).toFloat() / tone.durationSamples).coerceIn(0f, 1f)
                        // Make kick drums feel punched (quick exponential decay)
                        val envelope = if (tone.frequency < 150.0 && tone.type == WaveType.SINE) {
                            decayFactor * decayFactor // square curve for snappy drum decay
                        } else {
                            decayFactor
                        }

                        sampleSum += sampleValue * tone.volume * envelope
                        tone.currentSample++
                    }

                    // Soft clip output to prevent clipping distortion
                    sampleSum = sampleSum.coerceIn(-1.5, 1.5)
                    if (sampleSum > 1.0) sampleSum = 1.0 - 0.2 * (sampleSum - 1.0)
                    if (sampleSum < -1.0) sampleSum = -1.0 - 0.2 * (sampleSum + 1.0)

                    shortBuffer[i] = (sampleSum.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }

                audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                
                // If no active sounds are playing, small nap to save CPU
                if (activeCount == 0) {
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }.apply {
            name = "RhythmGameAudioThread"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    /**
     * Stop and release resources.
     */
    fun stop() {
        isRunning = false
        audioThread?.interrupt()
        audioThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Safe teardown
        }
        audioTrack = null
        activeTones.clear()
    }
}
