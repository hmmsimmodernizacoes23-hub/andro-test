package com.example.game

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.CustomSongRecord
import com.example.data.CustomSongRepository
import com.example.data.RhythmDatabase
import com.example.data.ScoreRecord
import com.example.data.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

enum class AppTheme(val id: String, val displayName: String) {
    DEEP_SPACE("deep_space", "Deep Space"),
    CYBERPUNK("cyberpunk", "Cyberpunk"),
    CHERRY_BLOSSOM("cherry_blossom", "Cherry Blossom"),
    RETRO_EMERALD("retro_emerald", "Retro Emerald"),
    MONOCHROME("monochrome", "Monochrome")
}

enum class AppLanguage(val id: String, val displayName: String) {
    EN("en", "English"),
    ES("es", "Español"),
    PT("pt", "Português"),
    JA("ja", "日本語"),
    DE("de", "Deutsch")
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val scoreRepository: ScoreRepository
    private val customSongRepository: CustomSongRepository
    val allScores: StateFlow<List<ScoreRecord>>

    private val prefs = application.getSharedPreferences("dfjk_rhythm_prefs", android.content.Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(
        AppTheme.values().firstOrNull { it.id == prefs.getString("theme", "deep_space") } ?: AppTheme.DEEP_SPACE
    )
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.values().firstOrNull { it.id == prefs.getString("language", "en") } ?: AppLanguage.EN
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun changeTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString("theme", theme.id).apply()
    }

    fun changeLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("language", language.id).apply()
    }

    init {
        val database = RhythmDatabase.getDatabase(application)
        scoreRepository = ScoreRepository(database.scoreDao())
        customSongRepository = CustomSongRepository(database.customSongDao())
        allScores = scoreRepository.allScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observe custom songs asynchronously and dynamically update the lists
        viewModelScope.launch {
            customSongRepository.allCustomSongs.collect { customRecords ->
                val presets = Song.createPresetSongs()
                val loadedCustom = customRecords.mapNotNull { record ->
                    try {
                        Song.parseFnfJson(record.jsonContent)
                    } catch (e: Exception) {
                        null
                    }
                }
                withContext(Dispatchers.Main) {
                    songs.clear()
                    songs.addAll(presets)
                    songs.addAll(loadedCustom)
                    
                    val currentSelected = _selectedSong.value
                    if (!songs.any { it.name == currentSelected.name }) {
                        songs.firstOrNull()?.let {
                            _selectedSong.value = it
                        }
                    }
                }
            }
        }
    }

    // --- Audio Engine ---
    private val audioEngine = AudioEngine()

    // --- Preset Songs & FNF Imports ---
    val songs = mutableStateListOf<Song>().apply {
        addAll(Song.createPresetSongs())
    }
    
    private val _selectedSong = MutableStateFlow(songs[0])
    val selectedSong: StateFlow<Song> = _selectedSong.asStateFlow()

    fun importFnfChart(jsonString: String): Boolean {
        return try {
            val customSong = Song.parseFnfJson(jsonString)
            viewModelScope.launch {
                customSongRepository.saveCustomSong(
                    CustomSongRecord(
                        songName = customSong.name,
                        jsonContent = jsonString
                    )
                )
                withContext(Dispatchers.Main) {
                    changeSelectedSong(customSong)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Local High Score for selected song ---
    private val _highScore = MutableStateFlow<ScoreRecord?>(null)
    val highScore: StateFlow<ScoreRecord?> = _highScore.asStateFlow()

    // --- Game States ---
    enum class GameState {
        TITLE, PLAYING, FAILED, RESULTS
    }

    private val _gameState = MutableStateFlow(GameState.TITLE)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _songTime = MutableStateFlow(0L)
    val songTime: StateFlow<Long> = _songTime.asStateFlow()

    // --- Score States ---
    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _combo = MutableStateFlow(0)
    val combo = _combo.asStateFlow()

    private val _maxCombo = MutableStateFlow(0)
    val maxCombo = _maxCombo.asStateFlow()

    private val _health = MutableStateFlow(1.0f) // Current bar [0.0 - 1.0]. Starts at 1.0f
    val health = _health.asStateFlow()

    // Counts of judgments
    private val _perfectCount = MutableStateFlow(0)
    val perfectCount = _perfectCount.asStateFlow()

    private val _greatCount = MutableStateFlow(0)
    val greatCount = _greatCount.asStateFlow()

    private val _goodCount = MutableStateFlow(0)
    val goodCount = _goodCount.asStateFlow()

    private val _missCount = MutableStateFlow(0)
    val missCount = _missCount.asStateFlow()

    // Accuracy Calculation
    private val _accuracy = MutableStateFlow(100f)
    val accuracy = _accuracy.asStateFlow()

    // Current playing notes checklist
    val pcmNotes = mutableStateListOf<GameNote>()

    // Lane Flash overlay states (last tap timestamp per lane)
    val laneFlashTime = mutableStateListOf(0L, 0L, 0L, 0L)

    // Lane touch pressed tracking states
    val lanePressed = mutableStateListOf(false, false, false, false)

    fun setLanePressed(lane: Int, pressed: Boolean) {
        if (lane in 0..3) {
            lanePressed[lane] = pressed
            if (pressed) {
                laneFlashTime[lane] = _songTime.value
            }
        }
    }

    // Floating notifications
    data class FloatingFeedback(
        val id: Long,
        val text: String,
        val color: String,
        val lane: Int,
        val scale: Float = 1.0f
    )
    val floatingFeedbacks = mutableStateListOf<FloatingFeedback>()
    private var feedbackCounter = 0L

    // Internal loop handling
    private var gameLoopJob: Job? = null
    private var audioScheduleIndex = 0

    fun changeSelectedSong(song: Song) {
        _selectedSong.value = song
        observeHighScoreForSelectedSong()
    }

    private var highScoreJob: Job? = null

    fun observeHighScoreForSelectedSong() {
        highScoreJob?.cancel()
        highScoreJob = viewModelScope.launch {
            scoreRepository.getHighScore(_selectedSong.value.name).collect { record ->
                _highScore.value = record
            }
        }
    }

    /**
     * Start the actual game session!
     */
    fun startGame() {
        _gameState.value = GameState.PLAYING
        _score.value = 0
        _combo.value = 0
        _maxCombo.value = 0
        _health.value = 1.0f
        _perfectCount.value = 0
        _greatCount.value = 0
        _goodCount.value = 0
        _missCount.value = 0
        _accuracy.value = 100f
        _songTime.value = 0L
        audioScheduleIndex = 0

        pcmNotes.clear()
        floatingFeedbacks.clear()
        
        // Load notes from selected beatmap
        val activeGameEvents = _selectedSong.value.gameNotes.map { event ->
            GameNote(
                id = event.id,
                lane = event.lane,
                hitTimeMs = event.hitTimeMs,
                holdDurationMs = event.holdDurationMs
            )
        }
        pcmNotes.addAll(activeGameEvents)

        audioEngine.start()
        
        val realStartTime = System.currentTimeMillis()
        val synthList = _selectedSong.value.synthNotes

        gameLoopJob = viewModelScope.launch {
            while (_gameState.value == GameState.PLAYING) {
                val nowTime = System.currentTimeMillis() - realStartTime
                _songTime.value = nowTime

                // 1. Play Synthesized Musical note events
                while (audioScheduleIndex < synthList.size && synthList[audioScheduleIndex].timeMs <= nowTime) {
                    val event = synthList[audioScheduleIndex]
                    audioEngine.playTone(
                        frequency = event.frequency,
                        type = event.type,
                        durationMs = event.durationMs,
                        volume = event.volume,
                        pitchSlideSpeed = event.pitchSlide
                    )
                    audioScheduleIndex++
                }

                // 2. Drive auto-miss evaluation for notes that sailed past hit threshold (150ms late)
                for (i in pcmNotes.indices) {
                    val note = pcmNotes.getOrNull(i) ?: continue
                    if (note.judgment == null && nowTime > note.hitTimeMs + 150) {
                        applyNoteJudgment(note, "MISS")
                    }
                }

                // 2.5 Hold Notes hold score ticks & premature release handling
                for (i in pcmNotes.indices) {
                    val note = pcmNotes.getOrNull(i) ?: continue
                    if (note.holdDurationMs > 0) {
                        val holdEndTime = note.hitTimeMs + note.holdDurationMs
                        if (note.isHit && !note.isHoldBroken && nowTime <= holdEndTime) {
                            if (!lanePressed[note.lane] && nowTime > note.hitTimeMs + 150) {
                                note.isHoldBroken = true
                                triggerFloatingText("RELEASE", "#FF88AA", note.lane, 0.8f)
                            } else if (lanePressed[note.lane]) {
                                _score.value += 1
                                if (nowTime % 120 < 10) {
                                    laneFlashTime[note.lane] = nowTime
                                }
                            }
                        }
                    }
                }

                // 3. Ending Condition
                if (nowTime >= _selectedSong.value.durationMs + 1000) {
                    endGameSuccess()
                    break
                }

                delay(5)
            }
        }
    }

    /**
     * Handle UI taps / physical keyboard events mapped into lane Indices [0..3]
     */
    fun registerLaneTap(lane: Int) {
        if (_gameState.value != GameState.PLAYING) return
        
        val hitTime = _songTime.value
        laneFlashTime[lane] = hitTime

        // Spark player sound feedback (Immediate retro triangle tone on hit)
        audioEngine.playTone(659.25, AudioEngine.WaveType.SINE, 50, volume = 0.12f)

        // Find earliest unhit note in lane within valid double 180ms window
        val note = pcmNotes.firstOrNull { n ->
            n.lane == lane && n.judgment == null && abs(hitTime - n.hitTimeMs) <= 150
        }

        if (note != null) {
            val offset = abs(hitTime - note.hitTimeMs)
            val judgment = when {
                offset <= 45 -> "PERFECT"
                offset <= 85 -> "GREAT"
                offset <= 140 -> "GOOD"
                else -> "MISS"
            }
            applyNoteJudgment(note, judgment)
        }
    }

    private fun applyNoteJudgment(note: GameNote, judgment: String) {
        note.judgment = judgment
        if (judgment != "MISS") {
            note.isHit = true
        }

        when (judgment) {
            "PERFECT" -> {
                _perfectCount.value++
                _combo.value++
                _score.value += 100 + (_combo.value / 10).coerceAtMost(20)
                _health.value = (_health.value + 0.04f).coerceAtMost(1.0f)
                triggerFloatingText("PERFECT", "#00FFDD", note.lane, 1.15f) // Glowing cyan
            }
            "GREAT" -> {
                _greatCount.value++
                _combo.value++
                _score.value += 70 + (_combo.value / 10).coerceAtMost(15)
                _health.value = (_health.value + 0.02f).coerceAtMost(1.0f)
                triggerFloatingText("GREAT", "#00FFAE", note.lane, 1.0f) // Neon green-cyan
            }
            "GOOD" -> {
                _goodCount.value++
                _combo.value++
                _score.value += 40 + (_combo.value / 10).coerceAtMost(10)
                _health.value = (_health.value + 0.01f).coerceAtMost(1.0f)
                triggerFloatingText("GOOD", "#FFBB00", note.lane, 0.9f) // Bright Orange
            }
            "MISS" -> {
                _missCount.value++
                _combo.value = 0
                _health.value = (_health.value - 0.08f).coerceAtLeast(0f)
                triggerFloatingText("MISS", "#FF2255", note.lane, 1.0f) // Red-Pink

                // Synthesize retro distortion drop for miss penalty!
                audioEngine.playTone(85.0, AudioEngine.WaveType.NOISE, 150, volume = 0.2f)

                if (_health.value <= 0.001f) {
                    terminateGameOver()
                }
            }
        }

        if (_combo.value > _maxCombo.value) {
            _maxCombo.value = _combo.value
        }

        recalculateAccuracy()
    }

    private fun triggerFloatingText(text: String, color: String, lane: Int, scale: Float) {
        val id = feedbackCounter++
        val feedback = FloatingFeedback(id, text, color, lane, scale)
        floatingFeedbacks.add(feedback)
        
        // Clean feedback after 450ms animation
        viewModelScope.launch {
            delay(450)
            floatingFeedbacks.remove(feedback)
        }
    }

    private fun recalculateAccuracy() {
        val totalNoteHits = _perfectCount.value + _greatCount.value + _goodCount.value + _missCount.value
        if (totalNoteHits == 0) {
            _accuracy.value = 100f
            return
        }
        val possibleScore = totalNoteHits * 100.0
        val earnedScore = (_perfectCount.value * 100.0) + (_greatCount.value * 70.0) + (_goodCount.value * 40.0)
        _accuracy.value = ((earnedScore / possibleScore) * 100.0).toFloat().coerceIn(0f, 100f)
    }

    private fun terminateGameOver() {
        if (_gameState.value != GameState.PLAYING) return
        _gameState.value = GameState.FAILED
        gameLoopJob?.cancel()
        gameLoopJob = null
        
        // Play fail sound directly on the main, running audioEngine!
        audioEngine.playTone(180.0, AudioEngine.WaveType.SAWTOOTH, 500, volume = 0.3f, pitchSlideSpeed = -300.0)
        
        viewModelScope.launch {
            delay(600)
            if (_gameState.value == GameState.FAILED) {
                audioEngine.stop()
            }
        }
    }

    private fun endGameSuccess() {
        if (_gameState.value != GameState.PLAYING) return
        _gameState.value = GameState.RESULTS
        gameLoopJob?.cancel()
        gameLoopJob = null

        // Celebrate success sound: retro hyper arpeggio directly on the main, running audioEngine!
        viewModelScope.launch {
            val scale = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C major arpeggio
            for (noteFreq in scale) {
                audioEngine.playTone(noteFreq, AudioEngine.WaveType.SQUARE, 120, volume = 0.25f)
                delay(120)
            }
            delay(400)
            if (_gameState.value == GameState.RESULTS) {
                audioEngine.stop()
            }
        }

        // Calculate final grade
        val rankStr = calculateFinalRank()

        // Save progress details to persistent Room Database
        val currentSong = _selectedSong.value
        val finalScoreVal = _score.value
        val maxComboVal = _maxCombo.value
        val perf = _perfectCount.value
        val grt = _greatCount.value
        val gd = _goodCount.value
        val mss = _missCount.value
        val acc = _accuracy.value

        viewModelScope.launch(Dispatchers.IO) {
            val record = ScoreRecord(
                songName = currentSong.name,
                difficulty = currentSong.difficulty,
                score = finalScoreVal,
                maxCombo = maxComboVal,
                perfectCount = perf,
                greatCount = grt,
                goodCount = gd,
                missCount = mss,
                rank = rankStr,
                accuracy = acc
            )
            scoreRepository.saveScore(record)
        }
    }

    fun calculateFinalRank(): String {
        val acc = _accuracy.value
        val isFullCombo = _missCount.value == 0 && (_perfectCount.value + _greatCount.value + _goodCount.value) > 0
        return when {
            acc >= 98f && isFullCombo -> "S"
            acc >= 94f -> "A"
            acc >= 85f -> "B"
            acc >= 75f -> "C"
            acc >= 60f -> "D"
            else -> "F"
        }
    }

    fun exitToTitle() {
        _gameState.value = GameState.TITLE
        stopSongAudio()
    }

    private fun stopSongAudio() {
        gameLoopJob?.cancel()
        gameLoopJob = null
        audioEngine.stop()
    }

    fun clearAllHighScores() {
        viewModelScope.launch(Dispatchers.IO) {
            scoreRepository.clearScores()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSongAudio()
    }
}

data class GameNote(
    val id: Int,
    val lane: Int,
    val hitTimeMs: Long,
    val holdDurationMs: Long = 0L,
    var isHit: Boolean = false,
    var judgment: String? = null,
    var isHoldBroken: Boolean = false
)
