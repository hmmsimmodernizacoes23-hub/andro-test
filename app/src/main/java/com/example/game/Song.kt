package com.example.game

import com.example.audio.AudioEngine
import org.json.JSONObject

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
    val hitTimeMs: Long,
    val holdDurationMs: Long = 0L
)

class Song(
    val name: String,
    val bpm: Int,
    val difficulty: String,
    val durationMs: Long,
    val synthNotes: List<SynthNoteEvent>,
    val gameNotes: List<GameNoteEvent>,
    val audioOggUri: String? = null
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
                generateProceduralSong("STARDUST GALLOP", 135, "Easy"),
                generateProceduralSong("VOLTAGE SURGE", 145, "Normal"),
                generateProceduralSong("GLITCHED MEMORIES", 152, "Normal"),
                generateProceduralSong("INFINITY OVERRIDE", 172, "Hard"),
                generateProceduralSong("COSMIC REVOLUTION", 185, "Hard"),
                parseFnfJson(FnfPresetCharts.TUTORIAL_FNF),
                parseFnfJson(FnfPresetCharts.BOPEEBO_FNF),
                parseFnfJson(FnfPresetCharts.DADBATTLE_FNF)
            )
        }

        fun parseFnfJson(jsonString: String, audioOggUri: String? = null): Song {
            val root = JSONObject(jsonString)
            val songObj = root.optJSONObject("song") ?: root
            val songName = songObj.optString("song", "FNF Custom").uppercase()
            val bpm = songObj.optInt("bpm", 150)
            
            val gameEvents = mutableListOf<GameNoteEvent>()
            val synthEvents = mutableListOf<SynthNoteEvent>()
            
            var noteIdCounter = 0
            val sectionsArray = songObj.optJSONArray("notes")
            
            if (sectionsArray != null) {
                for (s in 0 until sectionsArray.length()) {
                    val section = sectionsArray.optJSONObject(s) ?: continue
                    val mustHit = section.optBoolean("mustHitSection", true)
                    val sectionNotes = section.optJSONArray("sectionNotes") ?: continue
                    
                    for (n in 0 until sectionNotes.length()) {
                        val noteArr = sectionNotes.optJSONArray(n) ?: continue
                        if (noteArr.length() >= 2) {
                            val timeMs = noteArr.optDouble(0).toLong()
                            val rawLane = noteArr.optInt(1)
                            val sustainMs = if (noteArr.length() >= 3) noteArr.optDouble(2).toLong() else 0L
                            
                            val isPlayer: Boolean
                            val mappedLane: Int
                            
                            if (rawLane in 0..7) {
                                if (mustHit) {
                                    isPlayer = rawLane < 4
                                    mappedLane = if (isPlayer) rawLane else rawLane - 4
                                } else {
                                    isPlayer = rawLane >= 4
                                    mappedLane = if (isPlayer) rawLane - 4 else rawLane
                                }
                            } else {
                                isPlayer = true
                                mappedLane = (rawLane % 4).coerceAtLeast(0)
                            }
                            
                            if (isPlayer) {
                                gameEvents.add(
                                    GameNoteEvent(
                                        id = noteIdCounter++,
                                        lane = mappedLane,
                                        hitTimeMs = timeMs,
                                        holdDurationMs = sustainMs
                                    )
                                )
                            }
                            
                            val frequency = when (mappedLane) {
                                0 -> 261.63 // C4
                                1 -> 293.66 // D4
                                2 -> 329.63 // E4
                                3 -> 392.00 // G4
                                else -> 440.00
                            }
                            
                            if (!isPlayer) {
                                synthEvents.add(
                                    SynthNoteEvent(
                                        timeMs = timeMs,
                                        frequency = frequency,
                                        type = AudioEngine.WaveType.SAWTOOTH,
                                        durationMs = if (sustainMs > 50) sustainMs.toInt() else 120,
                                        volume = 0.12f
                                    )
                                )
                            } else {
                                synthEvents.add(
                                    SynthNoteEvent(
                                        timeMs = timeMs,
                                        frequency = frequency * 1.5,
                                        type = AudioEngine.WaveType.SINE,
                                        durationMs = 80,
                                        volume = 0.04f
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            val finalSongDuration = if (gameEvents.isEmpty() && synthEvents.isEmpty()) {
                60000L
            } else {
                val maxGameTime = gameEvents.maxOfOrNull { it.hitTimeMs } ?: 0L
                val maxSynthTime = synthEvents.maxOfOrNull { it.timeMs } ?: 0L
                (maxOf(maxGameTime, maxSynthTime) + 2000L).coerceAtLeast(10000L)
            }
            
            val stepMs = (60000.0 / bpm / 2.0).toLong()
            val totalSteps = (finalSongDuration / stepMs).toInt()
            
            for (step in 0 until totalSteps) {
                val timeMs = step * stepMs
                if (timeMs >= finalSongDuration - 1500) break
                
                val stepInBar = step % 16
                
                if (stepInBar == 0 || stepInBar == 8) {
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = 90.0,
                            type = AudioEngine.WaveType.SINE,
                            durationMs = 150,
                            volume = 0.18f,
                            pitchSlide = -300.0
                        )
                    )
                }
                if (stepInBar == 4 || stepInBar == 12) {
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = 420.0,
                            type = AudioEngine.WaveType.NOISE,
                            durationMs = 60,
                            volume = 0.08f
                        )
                    )
                }
                
                if (stepInBar % 4 == 0) {
                    val rootBass = when ((step / 16) % 4) {
                        0 -> 110.0
                        1 -> 82.41
                        2 -> 98.00
                        3 -> 73.42
                        else -> 110.0
                    }
                    synthEvents.add(
                        SynthNoteEvent(
                            timeMs = timeMs,
                            frequency = rootBass,
                            type = AudioEngine.WaveType.TRIANGLE,
                            durationMs = (stepMs * 1.5).toInt(),
                            volume = 0.10f
                        )
                    )
                }
            }
            
            return Song(
                name = songName,
                bpm = bpm,
                difficulty = "FNF",
                durationMs = finalSongDuration,
                synthNotes = synthEvents.sortedBy { it.timeMs },
                gameNotes = gameEvents.sortedBy { it.hitTimeMs },
                audioOggUri = audioOggUri
            )
        }

        private fun generateProceduralSong(name: String, bpm: Int, difficulty: String): Song {
            val stepMs = (60000.0 / bpm / 2.0).toLong() // 8th note duration in ms
            val songDurationMs = 60000L // 1 minute long
            val totalSteps = (songDurationMs / stepMs).toInt()

            val synthEvents = mutableListOf<SynthNoteEvent>()
            val gameEvents = mutableListOf<GameNoteEvent>()
            var gameNoteId = 0

            // 1. CHORDS, SCALES and SOUND WAVES based on Song Name
            val chords: List<List<Int>>
            val bassScale: DoubleArray
            val melodyScale: DoubleArray
            val bassWave: AudioEngine.WaveType
            val melodyWave: AudioEngine.WaveType
            val bassVolume: Float
            val melodyVolume: Float

            when (name) {
                "NEON HORIZON" -> { // Cozy, warm 80s Outrun (Em -> C -> G -> D)
                    bassScale = doubleArrayOf(41.20, 49.00, 55.00, 61.74, 73.42, 82.41, 98.00, 110.00)
                    melodyScale = doubleArrayOf(164.81, 196.00, 220.00, 246.94, 293.66, 329.63, 392.00, 440.00, 493.88, 587.33, 659.25, 783.99)
                    chords = listOf(
                        listOf(0, 2, 4), // Em
                        listOf(1, 4, 6), // C
                        listOf(2, 5, 7), // G
                        listOf(3, 5, 6)  // D
                    )
                    bassWave = AudioEngine.WaveType.TRIANGLE
                    melodyWave = AudioEngine.WaveType.SINE
                    bassVolume = 0.16f
                    melodyVolume = 0.14f
                }
                "STARDUST GALLOP" -> { // Uplifting Major Key (F Major -> Bb -> C -> Dm)
                    bassScale = doubleArrayOf(43.65, 51.91, 58.27, 65.41, 73.42, 87.31, 98.00, 116.54)
                    melodyScale = doubleArrayOf(174.61, 207.65, 233.08, 261.63, 293.66, 349.23, 392.00, 466.16, 523.25, 587.33, 698.46, 783.99)
                    chords = listOf(
                        listOf(0, 3, 5), // F Major
                        listOf(2, 4, 7), // Bb Major
                        listOf(3, 5, 0), // C Major
                        listOf(4, 1, 6)  // D Minor
                    )
                    bassWave = AudioEngine.WaveType.SINE
                    melodyWave = AudioEngine.WaveType.TRIANGLE
                    bassVolume = 0.18f
                    melodyVolume = 0.13f
                }
                "VOLTAGE SURGE" -> { // High energy retro house tracker (Am -> F -> G -> Em)
                    bassScale = doubleArrayOf(55.00, 65.41, 73.42, 82.41, 98.00, 110.00, 130.81, 146.83)
                    melodyScale = doubleArrayOf(220.00, 261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00)
                    chords = listOf(
                        listOf(0, 2, 5), // Am
                        listOf(1, 3, 6), // F
                        listOf(2, 4, 7), // G
                        listOf(3, 4, 5)  // Em
                    )
                    bassWave = AudioEngine.WaveType.SAWTOOTH
                    melodyWave = AudioEngine.WaveType.SQUARE
                    bassVolume = 0.12f
                    melodyVolume = 0.08f
                }
                "GLITCHED MEMORIES" -> { // Somber, beautiful lofi/glitch (Fm -> Db -> Ab -> Eb)
                    bassScale = doubleArrayOf(43.65, 48.99, 58.27, 65.41, 69.30, 87.31, 97.99, 110.00)
                    melodyScale = doubleArrayOf(174.61, 195.99, 233.08, 261.63, 277.18, 349.23, 391.99, 440.00, 523.25, 554.37, 698.46, 783.99)
                    chords = listOf(
                        listOf(0, 2, 4), // Fm
                        listOf(1, 3, 5), // Db
                        listOf(2, 4, 6), // Ab
                        listOf(3, 1, 5)  // Eb
                    )
                    bassWave = AudioEngine.WaveType.TRIANGLE
                    melodyWave = AudioEngine.WaveType.SINE
                    bassVolume = 0.14f
                    melodyVolume = 0.10f
                }
                "INFINITY OVERRIDE" -> { // Superfast Technical Phrygian DnB
                    bassScale = doubleArrayOf(46.25, 49.00, 61.74, 69.30, 73.42, 82.41, 92.50)
                    melodyScale = doubleArrayOf(185.00, 196.00, 246.94, 277.18, 293.66, 329.63, 369.99, 392.00, 493.88, 554.37, 587.33, 659.25, 739.99)
                    chords = listOf(
                        listOf(0, 1, 4), // F#m-G
                        listOf(1, 3, 5),
                        listOf(2, 4, 6),
                        listOf(3, 5, 0)
                    )
                    bassWave = AudioEngine.WaveType.SQUARE
                    melodyWave = AudioEngine.WaveType.SAWTOOTH
                    bassVolume = 0.10f
                    melodyVolume = 0.06f
                }
                else -> { // COSMIC REVOLUTION - Boss metal theme at 185 BPM! (Bm -> G -> A -> F#m)
                    bassScale = doubleArrayOf(49.00, 55.00, 58.27, 65.41, 73.42, 82.41, 98.00)
                    melodyScale = doubleArrayOf(246.94, 277.18, 293.66, 329.63, 369.99, 392.00, 440.00, 493.88, 554.37, 587.33, 659.25, 739.99, 880.00)
                    chords = listOf(
                        listOf(0, 2, 4),
                        listOf(1, 3, 5),
                        listOf(2, 4, 0),
                        listOf(3, 2, 5)
                    )
                    bassWave = AudioEngine.WaveType.SAWTOOTH
                    melodyWave = AudioEngine.WaveType.SAWTOOTH
                    bassVolume = 0.09f
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
                when (name) {
                    "NEON HORIZON" -> { // Smooth lounge synthwave beat
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
                    "STARDUST GALLOP" -> { // Cozy, happy galloping classic chip drums
                        if (stepInBar == 0 || stepInBar == 6 || stepInBar == 8 || stepInBar == 14) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 120.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 110,
                                    volume = 0.18f,
                                    pitchSlide = -400.0 // Snappy kick
                                )
                            )
                        }
                        if (stepInBar % 4 == 2) { // Upbeat hat
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 1000.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 35,
                                    volume = 0.05f
                                )
                            )
                        }
                    }
                    "VOLTAGE SURGE" -> { // Four-on-the-floor driving house
                        if (stepInBar % 4 == 0) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 135.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 130,
                                    volume = 0.22f,
                                    pitchSlide = -750.0 // punchy kick
                                )
                            )
                        }
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
                    "GLITCHED MEMORIES" -> { // Somber lofi syncopated beats
                        if (stepInBar == 0 || stepInBar == 10) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 95.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 140,
                                    volume = 0.16f,
                                    pitchSlide = -300.0
                                )
                            )
                        }
                        if (stepInBar == 4 || stepInBar == 12) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 300.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 60,
                                    volume = 0.10f
                                )
                            )
                        }
                        if (stepInBar % 8 == 6) { // soft high beep glitch
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 1800.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 15,
                                    volume = 0.03f
                                )
                            )
                        }
                    }
                    "INFINITY OVERRIDE" -> { // Fast asymmetric Breakcore/DnB
                        if (stepInBar == 0 || stepInBar == 3 || stepInBar == 8 || stepInBar == 11) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 145.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 100,
                                    volume = 0.24f,
                                    pitchSlide = -950.0
                                )
                            )
                        }
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
                    else -> { // COSMIC REVOLUTION - Blistering double bass metal drums
                        if (stepInBar % 2 == 0) { // Heavy double pedal
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 150.0,
                                    type = AudioEngine.WaveType.SINE,
                                    durationMs = 80,
                                    volume = 0.26f,
                                    pitchSlide = -1100.0
                                )
                            )
                        }
                        if (stepInBar == 4 || stepInBar == 12) {
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 500.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 85,
                                    volume = 0.18f
                                )
                            )
                        }
                        if (stepInBar % 2 == 1) { // High-speed hihat crash
                            synthEvents.add(
                                SynthNoteEvent(
                                    timeMs = timeMs,
                                    frequency = 2500.0,
                                    type = AudioEngine.WaveType.NOISE,
                                    durationMs = 30,
                                    volume = 0.06f
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
                val rHold = Math.random()
                val proceduralHoldDuration = when (difficulty) {
                    "Easy" -> 0L
                    "Normal" -> if (rHold < 0.12) stepMs * 2 else 0L
                    else -> if (rHold < 0.18) stepMs * 3 else 0L
                }

                when (difficulty) {
                    "Easy" -> {
                        val spawnInterval = 8
                        if (step % spawnInterval == 0) {
                            val laneCycle = listOf(0, 1, 2, 3, 2, 1)
                            val laneIndex = (step / spawnInterval) % laneCycle.size
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = laneCycle[laneIndex], hitTimeMs = timeMs, holdDurationMs = 0L))
                        }
                    }
                    "Normal" -> {
                        val isQuarterBeat = stepInBar % 4 == 0
                        val isOffBeat = stepInBar % 4 == 2 && (step / 4) % 2 == 0
                        
                        if (isQuarterBeat) {
                            val laneCycle = listOf(0, 1, 2, 3, 2, 1, 3, 0)
                            val lane = laneCycle[(step / 4) % laneCycle.size]
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs, holdDurationMs = proceduralHoldDuration))
                        } else if (isOffBeat) {
                            val activeLane = (step / 4) % 4
                            val adjacentLane = (activeLane + 1) % 4
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = adjacentLane, hitTimeMs = timeMs, holdDurationMs = 0L))
                        }
                    }
                    else -> { // Hard
                        val matchesDnbKick = stepInBar == 0 || stepInBar == 3 || stepInBar == 8 || stepInBar == 11
                        val matchesSnare = stepInBar == 4 || stepInBar == 12
                        
                        if (matchesDnbKick) {
                            if (stepInBar == 0 && (bar % 4 == 0)) {
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = 0, hitTimeMs = timeMs, holdDurationMs = proceduralHoldDuration))
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = 3, hitTimeMs = timeMs, holdDurationMs = proceduralHoldDuration))
                            } else {
                                val laneCycle = listOf(1, 2, 0, 3)
                                val lane = laneCycle[(step) % laneCycle.size]
                                gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs, holdDurationMs = proceduralHoldDuration))
                            }
                        } else if (matchesSnare) {
                            val lane = if (stepInBar == 4) 1 else 2
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs, holdDurationMs = 0L))
                        } else if (stepInBar % 2 != 0 && Math.random() < 0.70) {
                            val lane = (step / 2) % 4
                            gameEvents.add(GameNoteEvent(id = gameNoteId++, lane = lane, hitTimeMs = timeMs, holdDurationMs = 0L))
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
