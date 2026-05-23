package com.example.game

import com.example.audio.AudioEngine

data class SynthNoteEvent(
    val timeMs: Long,
    val frequency: Double,
    val type: AudioEngine.WaveType,
    val durationMs: Int,
    val volume: Float,
    val pitchSlide: Double = 0.0
)

data class GameNoteEvent(
    val id: Int,
    val lane: Int, // 0=D, 1=F, 2=J, 3=K
    val hitTimeMs: Long
)

class Song(
    val name: String,
    val bpm: Int,
    val difficulty: String,
    val durationMs: Long,
    val synthNotes: List<SynthNoteEvent>,
    val gameNotes: List<GameNoteEvent>
) {
    companion object {
        // Pentatonic Scale: A, C, D, E, G
        // Bass octaves (2 and 3)
        private val BassScale = doubleArrayOf(55.00, 65.41, 73.42, 82.41, 98.00, 110.00, 130.81, 146.83, 164.81, 196.00)
        // Melody octaves (4 and 5)
        private val MelodyScale = doubleArrayOf(220.00, 261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00)

        fun createPresetSongs(): List<Song> {
            return listOf(
                generateProceduralSong("Neon Horizon", 120, "Easy"),
                generateProceduralSong("Voltage Surge", 140, "Normal"),
                generateProceduralSong("Infinity Override", 165, "Hard")
            )
        }

        private fun generateProceduralSong(name: String, bpm: Int, difficulty: String): Song {
            val stepMs = (60000.0 / bpm / 2.0).toLong() // 8th note duration in ms
            val songDurationMs = 60000L // 1 minute long
            val totalSteps = (songDurationMs / stepMs).toInt()

            val synthEvents = mutableListOf<SynthNoteEvent>()
            val gameEvents = mutableListOf<GameNoteEvent>()
            var gameNoteId = 0

            // Traditional chord chordProgression (4 bars of A minor, 4 bars of G major, etc.)
            // Each bar has 16 steps (8 beats)
            val chords = listOf(
                listOf(5, 7, 9), // A minor scale degrees
                listOf(1, 3, 5), // C major
                listOf(4, 6, 8), // G major
                listOf(3, 5, 7)  // E minor
            )

            for (step in 0 until totalSteps) {
                val timeMs = step * stepMs
                if (timeMs >= songDurationMs - 1500) break

                val bar = step / 16
                val stepInBar = step % 16
                val chordIndex = bar % chords.size
                val currentChordDegrees = chords[chordIndex]

                // --- 1. SYNTHESIZE AUDIO TRACK (Backing track) ---
                
                // Track A: DRUMS
                // Kick on step 0, 4, 8, 12 (Every quarter note)
                if (stepInBar % 4 == 0) {
                    // Kick Drum: Rapidly dropping pitch sine wave
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = 130.0,
                            type = AudioEngine.WaveType.SINE,
                            durationMs = 120,
                            volume = 0.22f,
                            pitchSlide = -700.0 // swift slide down to bass punch
                        )
                    )
                }

                // Snare Drum (Noise burst) on step 4, 12
                if (stepInBar == 4 || stepInBar == 12) {
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = 400.0,
                            type = AudioEngine.WaveType.NOISE,
                            durationMs = 80,
                            volume = 0.15f
                        )
                    )
                }

                // Hi-Hat (crisp short noise) on odd steps
                if (stepInBar % 2 != 0) {
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = 1000.0,
                            type = AudioEngine.WaveType.NOISE,
                            durationMs = 25,
                            volume = 0.05f
                        )
                    )
                }

                // Track B: BASSLINE (Retro Sawtooth or Square Wave)
                // Plays on even steps (Quarter note beats)
                if (stepInBar % 2 == 0) {
                    val rootDegree = currentChordDegrees[0] % BassScale.size
                    val bassFreq = BassScale[rootDegree]
                    val duration = (stepMs * 0.8).toInt()
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = bassFreq,
                            type = AudioEngine.WaveType.SAWTOOTH,
                            durationMs = duration,
                            volume = 0.12f
                        )
                    )
                }

                // Track C: MELODY (Sparkling Square, Sine, or Triangle wave arpeggios)
                // Plays nice arpeggios
                val melodyProb = when (difficulty) {
                    "Easy" -> if (stepInBar % 4 == 0) 0.8 else 0.0
                    "Normal" -> if (stepInBar % 2 == 0) 0.7 else 0.2
                    else -> 0.8 // Hard plays continuous fast notes
                }

                if (Math.random() < melodyProb) {
                    // Choose note degree from chord notes
                    val chordNoteIndex = stepInBar % currentChordDegrees.size
                    var scaleDegree = currentChordDegrees[chordNoteIndex]
                    // Add some movement
                    if (stepInBar % 3 == 0) scaleDegree += 1
                    if (stepInBar % 7 == 0) scaleDegree -= 1
                    
                    val melodyDegree = scaleDegree.coerceIn(0, MelodyScale.lastIndex)
                    val melodyFreq = MelodyScale[melodyDegree]
                    val duration = (stepMs * 0.9).toInt()
                    
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = melodyFreq,
                            type = AudioEngine.WaveType.SQUARE,
                            durationMs = duration,
                            volume = 0.08f
                        )
                    )
                }

                // --- 2. GENERATE GAME NOTES MATCHING BEATS ---
                // We generate notes with accurate hitTimeMs = timeMs
                
                when (difficulty) {
                    "Easy" -> {
                        // Easy: note every 8 steps (one per 4 beats) or every 4 steps (one per 2 beats)
                        val spawnInterval = if (bar < 2) 16 else 8 // Starts very slow
                        if (step % spawnInterval == 0) {
                            // Single lane notes marching 0 -> 1 -> 2 -> 3 -> 2 -> 1 ...
                            val laneCycle = listOf(0, 1, 2, 3, 2, 1)
                            val laneIndex = (step / spawnInterval) % laneCycle.size
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = laneCycle[laneIndex], hitTimeMs = timeMs))
                        }
                    }
                    "Normal" -> {
                        // Normal: note every 4 steps (quarter note beats) or sometimes offbeat
                        val hasNote = step % 4 == 0 || (step % 4 == 2 && Math.random() < 0.4)
                        if (hasNote) {
                            // Cycle across lanes with some randomness
                            val lane = if (step % 8 == 0) {
                                (step / 8) % 4
                            } else {
                                ((step / 4) + 1) % 4
                            }
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs))
                        }
                    }
                    else -> { // Hard
                        // Hard: complex rhythms, double taps on beat 0/4, fast streams on melody
                        val isQuarterBeat = step % 4 == 0
                        val isOffBeat = step % 2 != 0

                        if (isQuarterBeat) {
                            // Randomly trigger double hits on major kick beats
                            if (step % 8 == 0 && Math.random() < 0.6) {
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = 0, hitTimeMs = timeMs))
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = 3, hitTimeMs = timeMs))
                            } else {
                                // Fast triplets or single hits
                                val lane = (step / 2) % 4
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs))
                            }
                        } else if (isOffBeat && Math.random() < 0.6) {
                            // Streams (e.g. 0 -> 1 -> 2 -> 3 cascading notes)
                            val lane = (step) % 4
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs))
                        }
                    }
                }
            }

            return Song(
                name = name,
                bpm = bpm,
                difficulty = difficulty,
                durationMs = songDurationMs,
                synthNotes = synthEvents.sortedBy { it.timeMs },
                gameNotes = gameEvents.sortedBy { it.hitTimeMs }
            )
        }
    }
}
