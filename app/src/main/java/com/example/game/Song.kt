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
                generateProceduralSong("NEON HORIZON", 120, "Easy"),
                generateProceduralSong("VOLTAGE SURGE", 140, "Normal"),
                generateProceduralSong("INFINITY OVERRIDE", 165, "Hard")
            )
        }

        private fun generateProceduralSong(name: String, bpm: Int, difficulty: String): Song {
            val stepMs = (60000.0 / bpm / 2.0).toLong() // 8th note duration in ms
            val songDurationMs = 60000L // 1 minute long
            val totalSteps = (songDurationMs / stepMs).toInt()

            val synthEvents = mutableListOf<SynthNoteEvent>()
            val gameEvents = mutableListOf<GameNoteEvent>()
            var gameNoteId = 0

            // 1. CHORDS, SCALES and SOUND WAVES based on Song/Difficulty
            val chords: List<List<Int>>
            val bassScale: DoubleArray
            val melodyScale: DoubleArray
            val bassWave: AudioEngine.WaveType
            val melodyWave: AudioEngine.WaveType
            val bassVolume: Float
            val melodyVolume: Float

            when (difficulty) {
                "Easy" -> { // NEON HORIZON - Cyberpunk Synthwave Vibe (G Major/E Minor)
                    // Scales (Warm & uplifting G Major/E Minor Pentatonic)
                    bassScale = doubleArrayOf(41.20, 49.00, 55.00, 61.74, 73.42, 82.41, 98.00, 110.00)
                    melodyScale = doubleArrayOf(164.81, 196.00, 220.00, 246.94, 293.66, 329.63, 392.00, 440.00, 493.88, 587.33, 659.25, 783.99)
                    
                    // Progressive dreamy progression (Em -> C -> G -> D)
                    chords = listOf(
                        listOf(0, 2, 4), // E, B, E2
                        listOf(1, 4, 6), // G, D, G2
                        listOf(2, 5, 7), // A, E2, A2
                        listOf(3, 5, 6)  // B, E2, G2
                    )
                    
                    bassWave = AudioEngine.WaveType.TRIANGLE // Smooth warm triangle bass
                    melodyWave = AudioEngine.WaveType.SINE     // Liquid sine melody
                    bassVolume = 0.16f
                    melodyVolume = 0.14f
                }
                "Normal" -> { // VOLTAGE SURGE - Energetic electro chiptune (A Minor / Dorian)
                    // Scales (Mysterious, punchy A Minor/Dorian)
                    bassScale = doubleArrayOf(55.00, 65.41, 73.42, 82.41, 98.00, 110.00, 130.81, 146.83)
                    melodyScale = doubleArrayOf(220.00, 261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00)
                    
                    // Driving energetic chord progression (Am -> F -> Dm -> Em)
                    chords = listOf(
                        listOf(0, 2, 5), // A, D, A2
                        listOf(1, 3, 6), // C, G, C2
                        listOf(2, 4, 7), // D, A, D2
                        listOf(3, 4, 5)  // E, A, A2
                    )
                    
                    bassWave = AudioEngine.WaveType.SAWTOOTH // Buzzing punchy bass
                    melodyWave = AudioEngine.WaveType.SQUARE    // Crispy classic chip-melody
                    bassVolume = 0.11f
                    melodyVolume = 0.08f
                }
                else -> { // INFINITY OVERRIDE - Phrygian Supersaw/Drum-and-Bass speed metal
                    // Scales (Dark, technical, high-voltage Phrygian)
                    bassScale = doubleArrayOf(46.25, 49.00, 61.74, 69.30, 73.42, 82.41, 92.50)
                    melodyScale = doubleArrayOf(185.00, 196.00, 246.94, 277.18, 293.66, 329.63, 369.99, 392.00, 493.88, 554.37, 587.33, 659.25, 739.99)
                    
                    // Dramatic tense progression (F#m -> G -> D#dim -> C#)
                    chords = listOf(
                        listOf(0, 1, 4), // F#, G, D
                        listOf(1, 3, 5), // G, C#, E
                        listOf(2, 4, 6), // B, D, F#
                        listOf(3, 5, 0)  // C#, E, F#
                    )
                    
                    bassWave = AudioEngine.WaveType.SQUARE   // Brutal thick square bass
                    melodyWave = AudioEngine.WaveType.SAWTOOTH // Supersonic laser supersaw
                    bassVolume = 0.10f
                    melodyVolume = 0.06f
                }
            }

            for (step in 0 until totalSteps) {
                val timeMs = step * stepMs
                if (timeMs >= songDurationMs - 1500) break

                val bar = step / 16
                val stepInBar = step % 16
                val chordIndex = bar % chords.size
                val currentChordDegrees = chords[chordIndex]

                // --- 1. SYNTHESIZE AUDIO TRACK (Backing track style varies by song) ---
                
                // Track A: DRUMS
                when (difficulty) {
                    "Easy" -> { // NEON HORIZON: Smooth synthwave beat
                        // Gentle Kick on step 0 and 8 only (Half-time tempo groove, very chill)
                        if (stepInBar == 0 || stepInBar == 8) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 110.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 180,
                                    volume = 0.20f,
                                    pitchSlide = -450.0 // gentle sliding kick
                                )
                            )
                        }
                        // Soft noise hi-hat on every 4th step (steps 4, 12)
                        if (stepInBar == 4 || stepInBar == 12) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 900.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 40,
                                    volume = 0.04f
                                )
                            )
                        }
                    }
                    "Normal" -> { // VOLTAGE SURGE: Classic four-on-the-floor house
                        // Kick on 0, 4, 8, 12
                        if (stepInBar % 4 == 0) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 135.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 130,
                                    volume = 0.22f,
                                    pitchSlide = -750.0 // crisp punchy transient
                                )
                            )
                        }
                        // Snare on 4, 12
                        if (stepInBar == 4 || stepInBar == 12) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 380.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 90,
                                    volume = 0.14f
                                )
                            )
                        }
                        // Upbeat Hi-Hats on 2, 6, 10, 14 (Creates energy/groove)
                        if (stepInBar % 4 == 2) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 1200.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 45,
                                    volume = 0.07f
                                )
                            )
                        }
                    }
                    else -> { // INFINITY OVERRIDE: Fast, intense Chiptune Breakcore/DnB
                        // Fast asymmetric DnB kick: step 0, 3, 8, 11
                        if (stepInBar == 0 || stepInBar == 3 || stepInBar == 8 || stepInBar == 11) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 145.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 100,
                                    volume = 0.24f,
                                    pitchSlide = -950.0 // ultra snappy laser drop
                                )
                            )
                        }
                        // Rapid snare hits (Heavy breakbeat): 4, 12, 14
                        if (stepInBar == 4 || stepInBar == 12 || stepInBar == 14) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 420.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 70,
                                    volume = 0.16f
                                )
                            )
                        }
                        // High speed double-time hats on every odd step
                        if (stepInBar % 2 != 0) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 2000.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 20,
                                    volume = 0.05f
                                )
                            )
                        }
                    }
                }

                // Track B: BASSLINE
                var playBass = false
                var bassStepIndex = 0
                when (difficulty) {
                    "Easy" -> {
                        if (stepInBar == 0 || stepInBar == 8) {
                            playBass = true
                            bassStepIndex = 0
                        }
                    }
                    "Normal" -> {
                        if (stepInBar % 2 == 0) {
                            playBass = true
                            bassStepIndex = stepInBar / 2
                        }
                    }
                    else -> {
                        if (stepInBar % 2 == 0 || (stepInBar % 4 == 1 && Math.random() < 0.6)) {
                            playBass = true
                            bassStepIndex = stepInBar
                        }
                    }
                }

                if (playBass) {
                    val rootDegree = (currentChordDegrees[bassStepIndex % currentChordDegrees.size]) % bassScale.size
                    val bassFreq = bassScale[rootDegree]
                    val duration = if (difficulty == "Easy") (stepMs * 1.6).toInt() else (stepMs * 0.75).toInt()
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = bassFreq,
                            type = bassWave,
                            durationMs = duration,
                            volume = bassVolume
                        )
                    )
                }

                // Track C: MELODY (Sparkling chiptune lines!)
                val playMelody = when (difficulty) {
                    "Easy" -> stepInBar % 8 == 0 // slow, beautiful arpeggio notes
                    "Normal" -> stepInBar % 4 == 0 || (stepInBar % 4 == 2 && Math.random() < 0.3) // steady melodic flow
                    else -> stepInBar % 2 == 0 || Math.random() < 0.5 // blazing fast blazing 16th-note lead lines!
                }

                if (playMelody) {
                    val melodyDegreeIndex: Int
                    val pitchSlideVal: Double
                    
                    when (difficulty) {
                        "Easy" -> {
                            val progressionOrder = listOf(0, 2, 4, 2, 3, 5, 7, 5)
                            melodyDegreeIndex = progressionOrder[(step / 8) % progressionOrder.size]
                            pitchSlideVal = 0.0
                        }
                        "Normal" -> {
                            val progressionOrder = listOf(2, 4, 6, 7, 5, 3, 4, 3)
                            var baseDegree = progressionOrder[(step / 4) % progressionOrder.size]
                            if (stepInBar % 8 == 4) baseDegree += 3
                            melodyDegreeIndex = baseDegree
                            pitchSlideVal = 0.0
                        }
                        else -> {
                            val baseDegree = (step % 12) + (if (Math.random() < 0.2) 2 else 0)
                            melodyDegreeIndex = baseDegree
                            pitchSlideVal = if (step % 16 == 0) -800.0 else if (step % 24 == 12) 600.0 else 0.0
                        }
                    }

                    val melodyDegree = melodyDegreeIndex.coerceIn(0, melodyScale.lastIndex)
                    val melodyFreq = melodyScale[melodyDegree]
                    val duration = if (difficulty == "Easy") (stepMs * 1.8).toInt() else (stepMs * 0.85).toInt()
                    
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = melodyFreq,
                            type = melodyWave,
                            durationMs = duration,
                            volume = melodyVolume,
                            pitchSlide = pitchSlideVal
                        )
                    )
                }

                // --- 2. GENERATE GAME NOTES MATCHING BEATS ---
                when (difficulty) {
                    "Easy" -> {
                        val spawnInterval = 8
                        if (step % spawnInterval == 0) {
                            val laneCycle = listOf(0, 1, 2, 3, 2, 1)
                            val laneIndex = (step / spawnInterval) % laneCycle.size
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = laneCycle[laneIndex], hitTimeMs = timeMs))
                        }
                    }
                    "Normal" -> {
                        val isQuarterBeat = stepInBar % 4 == 0
                        val isOffBeat = stepInBar % 4 == 2 && (step / 4) % 2 == 0
                        
                        if (isQuarterBeat) {
                            val laneCycle = listOf(0, 1, 2, 3, 2, 1, 3, 0)
                            val lane = laneCycle[(step / 4) % laneCycle.size]
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs))
                        } else if (isOffBeat) {
                            val activeLane = (step / 4) % 4
                            val adjacentLane = (activeLane + 1) % 4
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = adjacentLane, hitTimeMs = timeMs))
                        }
                    }
                    else -> { // Hard
                        val matchesDnbKick = stepInBar == 0 || stepInBar == 3 || stepInBar == 8 || stepInBar == 11
                        val matchesSnare = stepInBar == 4 || stepInBar == 12
                        
                        if (matchesDnbKick) {
                            if (stepInBar == 0 && (bar % 4 == 0)) {
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = 0, hitTimeMs = timeMs))
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = 3, hitTimeMs = timeMs))
                            } else {
                                val laneCycle = listOf(1, 2, 0, 3)
                                val lane = laneCycle[(step) % laneCycle.size]
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs))
                            }
                        } else if (matchesSnare) {
                            val lane = if (stepInBar == 4) 1 else 2
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs))
                        } else if (stepInBar % 2 != 0 && Math.random() < 0.70) {
                            val lane = (step / 2) % 4
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
