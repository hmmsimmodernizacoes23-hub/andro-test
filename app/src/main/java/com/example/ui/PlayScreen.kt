package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.ScoreRecord
import com.example.game.GameViewModel
import com.example.game.Song
import com.example.game.AppTheme
import com.example.game.AppLanguage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Dynamic Theme System and Localizations
data class ThemeColors(
    val bg: Color,
    val cardBg: Color,
    val accentCyan: Color,  // lane 1
    val accentPink: Color,  // lane 2
    val accentLavender: Color, // lane 0 / generic highlight
    val accentGreen: Color, // lane 3 / successes
    val lightAccentText: Color,
    val textLight: Color,
    val keyPassiveBg: Color
)

object ThemeManager {
    fun getColors(theme: AppTheme): ThemeColors {
        return when (theme) {
            AppTheme.DEEP_SPACE -> ThemeColors(
                bg = Color(0xFF131118),
                cardBg = Color(0xFF211F26),
                accentCyan = Color(0xFF80DEEA),
                accentPink = Color(0xFFFF8A80),
                accentLavender = Color(0xFFD0BCFE),
                accentGreen = Color(0xFFA5D6A7),
                lightAccentText = Color(0xFFCCC2DC),
                textLight = Color(0xFFE6E1E5),
                keyPassiveBg = Color(0xFF49454F)
            )
            AppTheme.CYBERPUNK -> ThemeColors(
                bg = Color(0xFF0F0C1B),
                cardBg = Color(0xFF1E152E),
                accentCyan = Color(0xFF00E5FF),
                accentPink = Color(0xFFFF007F),
                accentLavender = Color(0xFFFFEA00),
                accentGreen = Color(0xFF39FF14),
                lightAccentText = Color(0xFFFFEA00),
                textLight = Color(0xFFF3E5F5),
                keyPassiveBg = Color(0xFF381F4B)
            )
            AppTheme.CHERRY_BLOSSOM -> ThemeColors(
                bg = Color(0xFFFFF5F6),
                cardBg = Color(0xFFFCE4EC),
                accentCyan = Color(0xFF81D4FA),
                accentPink = Color(0xFFFF4081),
                accentLavender = Color(0xFF9C27B0),
                accentGreen = Color(0xFF4CAF50),
                lightAccentText = Color(0xFF8D6E63),
                textLight = Color(0xFF3E2723),
                keyPassiveBg = Color(0xFFE1BEE7)
            )
            AppTheme.RETRO_EMERALD -> ThemeColors(
                bg = Color(0xFF081C0E),
                cardBg = Color(0xFF15331C),
                accentCyan = Color(0xFFA9DFBF),
                accentPink = Color(0xFF58D68D),
                accentLavender = Color(0xFFCCFF90),
                accentGreen = Color(0xFF2ECC71),
                lightAccentText = Color(0xFF9FD6B2),
                textLight = Color(0xFFE8F5E9),
                keyPassiveBg = Color(0xFF1E4227)
            )
            AppTheme.MONOCHROME -> ThemeColors(
                bg = Color(0xFF121212),
                cardBg = Color(0xFF222222),
                accentCyan = Color(0xFFCCCCCC),
                accentPink = Color(0xFFFFFFFF),
                accentLavender = Color(0xFFE0E0E0),
                accentGreen = Color(0xFF999999),
                lightAccentText = Color(0xFF888888),
                textLight = Color(0xFFF5F5F5),
                keyPassiveBg = Color(0xFF3A3A3A)
            )
        }
    }
}

object TranslationManager {
    private val translations = mapOf(
        AppLanguage.EN to mapOf(
            "choose_song" to "CHOOSE SONG",
            "tap_to_play" to "START PLAYING",
            "select_diff" to "CHOOSE DIFFICULTY",
            "clear_records" to "CLEAR RECORDS",
            "confirm_clear" to "Are you sure you want to clear all high scores?",
            "yes" to "YES, CLEAR",
            "cancel" to "CANCEL",
            "high_score_title" to "Best Record",
            "no_scores" to "No play history recorded yet.",
            "total_score" to "Total Score",
            "max_combo" to "Max Combo",
            "accuracy" to "Accuracy",
            "perfects" to "Perfects",
            "greats" to "Greats",
            "goods" to "Goods",
            "misses" to "Misses",
            "retry" to "REPLAY SONG",
            "exit" to "BACK TO TITLE",
            "perfect" to "PERFECT",
            "great" to "GREAT",
            "good" to "GOOD",
            "miss" to "MISS",
            "failed" to "STAGE FAILED",
            "results" to "STAGE CLEAR",
            "rank" to "Rank",
            "history" to "RECENT PLAY HISTORIES",
            "bpm" to "BPM",
            "notes" to "Notes",
            "easy" to "Easy",
            "normal" to "Normal",
            "hard" to "Hard",
            "settings" to "System Settings"
        ),
        AppLanguage.ES to mapOf(
            "choose_song" to "ELEGIR CANCIÓN",
            "tap_to_play" to "EMPEZAR JUEGO",
            "select_diff" to "ELEGIR DIFICULTAD",
            "clear_records" to "BORRAR HISTORIAL",
            "confirm_clear" to "¿Estás seguro de que deseas borrar los puntajes?",
            "yes" to "SÍ, BORRAR",
            "cancel" to "CANCELAR",
            "high_score_title" to "Récord Máximo",
            "no_scores" to "No hay puntajes guardados aún.",
            "total_score" to "Puntaje Total",
            "max_combo" to "Combo Máximo",
            "accuracy" to "Precisión",
            "perfects" to "Perfectos",
            "greats" to "Grandiosos",
            "goods" to "Buenos",
            "misses" to "Fallos",
            "retry" to "REINTENTAR CANCIÓN",
            "exit" to "SALIR AL TITLE",
            "perfect" to "PERFECTO",
            "great" to "GRANDIOSO",
            "good" to "BUENO",
            "miss" to "FALLO",
            "failed" to "INTENTO FALLADO",
            "results" to "FASE COMPLETADA",
            "rank" to "Rango",
            "history" to "HISTORIAL RECIENTE",
            "bpm" to "BPM",
            "notes" to "Notas",
            "easy" to "Fácil",
            "normal" to "Normal",
            "hard" to "Difícil",
            "settings" to "Ajustes del Sistema"
        ),
        AppLanguage.PT to mapOf(
            "choose_song" to "ESCOLHER MÚSICA",
            "tap_to_play" to "COMEÇAR JOGADA",
            "select_diff" to "CHOOSE DIFFICULTY",
            "clear_records" to "APAGAR RECORDE",
            "confirm_clear" to "Apagar logs de pontuação para sempre?",
            "yes" to "SIM, APAGAR",
            "cancel" to "CANCELAR",
            "high_score_title" to "Melhor Recorde",
            "no_scores" to "Histórico de jogo vazio.",
            "total_score" to "Pontos Totais",
            "max_combo" to "Combo Máximo",
            "accuracy" to "Precisão",
            "perfects" to "Perfeitos",
            "greats" to "Excelentes",
            "goods" to "Bons",
            "misses" to "Erros",
            "retry" to "REPETIR MÚSICA",
            "exit" to "SAIR PARA O TITULO",
            "perfect" to "PERFEITO",
            "great" to "EXCELENTE",
            "good" to "BOM",
            "miss" to "ERRO",
            "failed" to "MÚSICA FALHOU",
            "results" to "MÚSICA CONCLUÍDA",
            "rank" to "Rank",
            "history" to "HISTORICO DE JOGOS",
            "bpm" to "BPM",
            "notes" to "Notas",
            "easy" to "Fácil",
            "normal" to "Normal",
            "hard" to "Difícil",
            "settings" to "Configurações"
        ),
        AppLanguage.JA to mapOf(
            "choose_song" to "楽曲を選択",
            "tap_to_play" to "プレイ開始",
            "select_diff" to "難易度設定",
            "clear_records" to "履歴を消去",
            "confirm_clear" to "すべてのハイスコアを消去しますか？",
            "yes" to "消去する",
            "cancel" to "キャンセル",
            "high_score_title" to "ベスト記録",
            "no_scores" to "プレイ履歴がまだありません。",
            "total_score" to "合計スコア",
            "max_combo" to "最大コンボ",
            "accuracy" to "正確度",
            "perfects" to "パーフェクト",
            "greats" to "グレート",
            "goods" to "グッド",
            "misses" to "ミス",
            "retry" to "もう一度プレイ",
            "exit" to "タイトルに戻る",
            "perfect" to "PERFECT",
            "great" to "GREAT",
            "good" to "GOOD",
            "miss" to "MISS",
            "failed" to "ゲームオーバー",
            "results" to "リザルト発表",
            "rank" to "ランク",
            "history" to "最近のプレイ履歴",
            "bpm" to "BPM",
            "notes" to "ノート数",
            "easy" to "かんたん",
            "normal" to "ふつう",
            "hard" to "むずかしい",
            "settings" to "システム設定"
        ),
        AppLanguage.DE to mapOf(
            "choose_song" to "SONG AUSWÄHLEN",
            "tap_to_play" to "SPIEL STARTEN",
            "select_diff" to "SCHWIERIGKEITSGRAD",
            "clear_records" to "HISTORIE LÖSCHEN",
            "confirm_clear" to "Möchten Sie alle Highscores wirklich löschen?",
            "yes" to "JA, LÖSCHEN",
            "cancel" to "ABBRECHEN",
            "high_score_title" to "Bestleistung",
            "no_scores" to "Noch keine Spieldaten vorhanden.",
            "total_score" to "Gesamtpunkte",
            "max_combo" to "Max Combo",
            "accuracy" to "Präzision",
            "perfects" to "Perfekt",
            "greats" to "Sehr Gut",
            "goods" to "Gut",
            "misses" to "Miss",
            "retry" to "LIED WIEDERHOLEN",
            "exit" to "ZUM TITEL",
            "perfect" to "PERFEKT",
            "great" to "SEHR GUT",
            "good" to "GUT",
            "miss" to "FEHLER",
            "failed" to "FEHLGESCHLAGEN",
            "results" to "STAGE ERFOLGREICH",
            "rank" to "Rang",
            "history" to "LETZTE RUNDEN",
            "bpm" to "BPM",
            "notes" to "Noten",
            "easy" to "Einfach",
            "normal" to "Normal",
            "hard" to "Schwer",
            "settings" to "System-Einstellungen"
        )
    )

    fun getString(key: String, lang: AppLanguage): String {
        return translations[lang]?.get(key) ?: translations[AppLanguage.EN]?.get(key) ?: key
    }
}

@Composable
fun RowScope.ThemeChip(
    theme: AppTheme,
    activeTheme: AppTheme,
    colors: ThemeColors,
    onClick: () -> Unit
) {
    val isSelected = (theme == activeTheme)
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.accentLavender else colors.keyPassiveBg.copy(alpha = 0.4f))
            .border(1.dp, if (isSelected) colors.accentLavender else colors.keyPassiveBg.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = theme.displayName.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            color = if (isSelected) colors.bg else colors.textLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MainRhythmApp(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val activeTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val colors = ThemeManager.getColors(activeTheme)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.bg
    ) {
        when (gameState) {
            GameViewModel.GameState.TITLE -> TitleScreen(viewModel)
            GameViewModel.GameState.PLAYING -> PlayScreen(viewModel)
            GameViewModel.GameState.FAILED -> FailedScreen(viewModel)
            GameViewModel.GameState.RESULTS -> ResultsScreen(viewModel)
        }
    }
}

/**
 * --- 1. TITLE SCREEN ---
 */
@Composable
fun TitleScreen(viewModel: GameViewModel) {
    val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
    val highScoreRecord by viewModel.highScore.collectAsStateWithLifecycle()
    val historyList by viewModel.allScores.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val activeTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    
    val colors = ThemeManager.getColors(activeTheme)
    val haptic = LocalHapticFeedback.current

    var showImportDialog by remember { mutableStateOf(false) }
    var pastedJsonText by remember { mutableStateOf("") }
    var audioOggInputPath by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var parseErrorMsg by remember { mutableStateOf<String?>(null) }
    var parseSuccessMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            var displayName = ""
            if (cursor != null && nameIndex != null && cursor.moveToFirst()) {
                displayName = cursor.getString(nameIndex)
            }
            cursor?.close()
            audioOggInputPath = if (displayName.isNotEmpty()) displayName else uri.toString()
        }
    }

    // Trigger initial observing hook
    LaunchedEffect(selectedSong) {
        viewModel.observeHighScoreForSelectedSong()
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                pastedJsonText = ""
                audioOggInputPath = ""
                selectedAudioUri = null
                parseErrorMsg = null
                parseSuccessMsg = null
            },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "IMPORT FNF CHART",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = colors.accentPink
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Paste FNF (Friday Night Funkin') style JSON chart data. We will dynamically synthesize play tracks and chiptunes based on notes & BPM!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textLight.copy(alpha = 0.8f)
                    )

                    if (parseErrorMsg != null) {
                        Text(
                            text = parseErrorMsg!!,
                            color = colors.accentPink,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (parseSuccessMsg != null) {
                        Text(
                            text = parseSuccessMsg!!,
                            color = colors.accentGreen,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedTextField(
                        value = pastedJsonText,
                        onValueChange = {
                            pastedJsonText = it
                            parseErrorMsg = null
                            parseSuccessMsg = null
                        },
                        placeholder = {
                            Text(
                                "{\"song\": {\"song\": \"Bopeebo\", \"bpm\": 115, ...}}",
                                color = colors.lightAccentText.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentLavender,
                            unfocusedBorderColor = colors.keyPassiveBg,
                            focusedTextColor = colors.textLight,
                            unfocusedTextColor = colors.textLight
                        ),
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Add custom .ogg audio (optional):",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.lightAccentText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = audioOggInputPath,
                            onValueChange = {
                                audioOggInputPath = it
                                selectedAudioUri = null // clear picker if typed URL instead
                            },
                            placeholder = {
                                Text(
                                    "Paste URL or browse local audio",
                                    color = colors.lightAccentText.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accentLavender,
                                unfocusedBorderColor = colors.keyPassiveBg,
                                focusedTextColor = colors.textLight,
                                unfocusedTextColor = colors.textLight
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                try {
                                    audioLauncher.launch("audio/*")
                                } catch (e: Exception) {
                                    parseErrorMsg = "Could not launch file picker."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.keyPassiveBg,
                                contentColor = colors.textLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .width(52.dp)
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Browse local files",
                                tint = colors.accentPink
                            )
                        }
                    }

                    Text(
                        text = "Or try one of our loaded charts:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.lightAccentText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                pastedJsonText = com.example.game.FnfPresetCharts.TUTORIAL_FNF
                                parseErrorMsg = null
                                parseSuccessMsg = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.keyPassiveBg,
                                contentColor = colors.textLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text("TUTORIAL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                pastedJsonText = com.example.game.FnfPresetCharts.BOPEEBO_FNF
                                parseErrorMsg = null
                                parseSuccessMsg = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.keyPassiveBg,
                                contentColor = colors.textLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text("BOPEEBO", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                pastedJsonText = com.example.game.FnfPresetCharts.DADBATTLE_FNF
                                parseErrorMsg = null
                                parseSuccessMsg = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.keyPassiveBg,
                                contentColor = colors.textLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text("DADBATTLE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedJsonText.isBlank()) {
                            parseErrorMsg = "Please paste or select a JSON chart first!"
                            return@Button
                        }

                        val parsedSongForName = try {
                            Song.parseFnfJson(pastedJsonText)
                        } catch (e: Exception) {
                            null
                        }

                        if (parsedSongForName == null) {
                            parseErrorMsg = "Error parsing FNF format. Please check syntax."
                            return@Button
                        }

                        var finalAudioUriString: String? = null
                        val uri = selectedAudioUri
                        if (uri != null) {
                            val localPath = viewModel.copyUriToLocalFile(uri, parsedSongForName.name)
                            if (localPath != null) {
                                finalAudioUriString = localPath
                            }
                        } else if (audioOggInputPath.isNotBlank()) {
                            finalAudioUriString = audioOggInputPath
                        }

                        val success = viewModel.importFnfChart(pastedJsonText, finalAudioUriString)
                        if (success) {
                            showImportDialog = false
                            pastedJsonText = ""
                            audioOggInputPath = ""
                            selectedAudioUri = null
                            parseErrorMsg = null
                            parseSuccessMsg = null
                        } else {
                            parseErrorMsg = "Error parsing FNF format. Please check syntax."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentLavender,
                        contentColor = colors.bg
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("PARSE & LOAD", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportDialog = false
                        pastedJsonText = ""
                        audioOggInputPath = ""
                        selectedAudioUri = null
                        parseErrorMsg = null
                        parseSuccessMsg = null
                    }
                ) {
                    Text("CANCEL", color = colors.lightAccentText, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = colors.bg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "DFJK RHYTHM",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                ),
                color = colors.textLight,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "CHIPTUNE SYNTH EDITION",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = colors.accentLavender,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 0: Settings Panel (Theme and Language Row selections)
                item {
                    Text(
                        text = TranslationManager.getString("settings", currentLanguage).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = colors.lightAccentText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Unified language/theme card panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.cardBg)
                            .border(1.dp, colors.keyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Language / Idioma / 言語",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textLight.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppLanguage.values().forEach { lang ->
                                val isSelected = (lang == currentLanguage)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) colors.accentLavender else colors.keyPassiveBg.copy(alpha = 0.4f))
                                        .border(1.dp, if (isSelected) colors.accentLavender else colors.keyPassiveBg.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.changeLanguage(lang)
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) colors.bg else colors.textLight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "App Color Theme",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textLight.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Theme Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val themes = AppTheme.values()
                            // First row: 3 themes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                themes.take(3).forEach { t ->
                                    ThemeChip(t, activeTheme, colors, onClick = {
                                        viewModel.changeTheme(t)
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    })
                                }
                            }
                            // Second row: 2 themes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                themes.drop(3).forEach { t ->
                                    ThemeChip(t, activeTheme, colors, onClick = {
                                        viewModel.changeTheme(t)
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Section 1: Song Selection
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TranslationManager.getString("choose_song", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = colors.textLight
                        )
                        
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                showImportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentPink.copy(alpha = 0.2f),
                                contentColor = colors.accentPink
                            ),
                            border = BorderStroke(1.dp, colors.accentPink.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Import FNF",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "IMPORT FNF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Display Preset Cards
                    viewModel.songs.forEach { song ->
                        SongCard(
                            song = song,
                            isSelected = song.name == selectedSong.name,
                            colors = colors,
                            currentLanguage = currentLanguage,
                            onClick = {
                                viewModel.changeSelectedSong(song)
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Section 2: Selected Song Score History / Record
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = TranslationManager.getString("high_score_title", currentLanguage).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.textLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardBg)
                            .border(1.dp, colors.keyPassiveBg.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        val record = highScoreRecord
                        if (record != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${TranslationManager.getString("high_score_title", currentLanguage)}: ${record.score}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colors.accentCyan
                                    )
                                    Text(
                                        text = "${TranslationManager.getString("max_combo", currentLanguage)}: x${record.maxCombo}  |  ${TranslationManager.getString("accuracy", currentLanguage)}: %.1f%%".format(record.accuracy),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.lightAccentText
                                    )
                                    Text(
                                        text = "${TranslationManager.getString("perfects", currentLanguage)}: ${record.perfectCount}  |  ${TranslationManager.getString("misses", currentLanguage)}: ${record.missCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.lightAccentText.copy(alpha = 0.8f)
                                    )
                                }

                                // Large glowing rank letter badge
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(colors.keyPassiveBg)
                                        .border(2.dp, colors.accentPink, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = record.rank,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = colors.lightAccentText,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = TranslationManager.getString("no_scores", currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.lightAccentText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Section 3: Historical Runs Leaderboard
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TranslationManager.getString("history", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = colors.textLight
                        )
                        
                        if (historyList.isNotEmpty()) {
                            Text(
                                text = TranslationManager.getString("clear_records", currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentPink
                                ),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.clearAllHighScores()
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(colors.cardBg)
                                .border(1.dp, colors.keyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = TranslationManager.getString("no_scores", currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.lightAccentText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(historyList) { record ->
                        HistoryRow(record, colors, currentLanguage)
                    }
                }
            }

            // Big Start CTA Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.startGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("start_game_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentLavender,
                    contentColor = colors.bg
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = TranslationManager.getString("tap_to_play", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SongCard(
    song: Song,
    isSelected: Boolean,
    colors: ThemeColors,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) colors.accentLavender else colors.keyPassiveBg.copy(alpha = 0.3f)
    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(colors.cardBg, colors.accentLavender.copy(alpha = 0.12f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(colors.cardBg, colors.cardBg)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundBrush)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) colors.keyPassiveBg else colors.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isSelected) colors.accentLavender else colors.lightAccentText
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${TranslationManager.getString("bpm", currentLanguage)} ${song.bpm}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.lightAccentText
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.lightAccentText
                    )
                    Text(
                        text = "${TranslationManager.getString("notes", currentLanguage)} ~${song.gameNotes.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.lightAccentText
                    )
                }
            }

            // Difficulty Badge Toggle
            val diffColor = when (song.difficulty) {
                "Easy" -> colors.accentGreen
                "Normal" -> Color(0xFFFFAA00)
                else -> colors.accentPink
            }

            val difficultyLabel = when (song.difficulty) {
                "Easy" -> TranslationManager.getString("easy", currentLanguage)
                "Normal" -> TranslationManager.getString("normal", currentLanguage)
                else -> TranslationManager.getString("hard", currentLanguage)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(diffColor.copy(alpha = 0.15f))
                    .border(1.dp, diffColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = difficultyLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    ),
                    color = diffColor
                )
            }
        }
    }
}

@Composable
fun HistoryRow(
    record: ScoreRecord,
    colors: ThemeColors,
    currentLanguage: AppLanguage
) {
    val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val dateStr = fmt.format(Date(record.timestamp))

    val diffColor = when (record.difficulty) {
        "Easy" -> colors.accentGreen
        "Normal" -> Color(0xFFFFAA00)
        else -> colors.accentPink
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBg.copy(alpha = 0.6f))
            .border(1.dp, colors.keyPassiveBg.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = record.songName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textLight
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val difficultyLabel = when (record.difficulty) {
                    "Easy" -> TranslationManager.getString("easy", currentLanguage)
                    "Normal" -> TranslationManager.getString("normal", currentLanguage)
                    else -> TranslationManager.getString("hard", currentLanguage)
                }
                Text(
                    text = difficultyLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = diffColor
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.lightAccentText.copy(alpha = 0.3f)
                )
                Text(
                    text = "x${record.maxCombo} Combo",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.lightAccentText
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.lightAccentText.copy(alpha = 0.3f)
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.lightAccentText.copy(alpha = 0.6f)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${record.score} pts",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = colors.accentCyan
            )
            Text(
                text = "GRADE ${record.rank}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                ),
                color = colors.accentPink
            )
        }
    }
}

/**
 * --- 2. GAME PLAYING SCREEN ---
 */
@Composable
fun PlayScreen(viewModel: GameViewModel) {
    val songTime by viewModel.songTime.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val combo by viewModel.combo.collectAsStateWithLifecycle()
    val maxCombo by viewModel.maxCombo.collectAsStateWithLifecycle()
    val health by viewModel.health.collectAsStateWithLifecycle()
    val accuracy by viewModel.accuracy.collectAsStateWithLifecycle()
    val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val activeTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    val colors = ThemeManager.getColors(activeTheme)
    val haptic = LocalHapticFeedback.current

    // Set constant target hit windows
    val spawnTimeMs = 1200L

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.bg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // HUD Top Panel
            HUDPanel(
                songName = selectedSong.name,
                difficulty = selectedSong.difficulty,
                score = score,
                accuracy = accuracy,
                health = health,
                songProgress = (songTime.toFloat() / selectedSong.durationMs).coerceIn(0f, 1f),
                colors = colors,
                currentLanguage = currentLanguage,
                onPauseClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.exitToTitle()
                }
            )

            // Dynamic Game Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Falling Notes Canvas
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val laneWidth = width / 4f
                    val hitLineY = height * 0.90f

                    // 1. Draw Lane board backgrounds
                    for (i in 0..3) {
                        val startX = i * laneWidth
                        
                        // Lane strip background
                        drawRect(
                            color = if (i % 2 == 0) colors.cardBg.copy(alpha = 0.35f) else colors.cardBg.copy(alpha = 0.2f),
                            topLeft = Offset(startX, 0f),
                            size = Size(laneWidth, height)
                        )

                        // Outer separator borders
                        if (i > 0) {
                            drawLine(
                                color = colors.keyPassiveBg.copy(alpha = 0.3f),
                                start = Offset(startX, 0f),
                                end = Offset(startX, height),
                                strokeWidth = 1.5f
                            )
                        }
                    }

                    // 2. Draw Hit target line glowing beam
                    val beamColor = Color.White
                    drawLine(
                        color = beamColor.copy(alpha = 0.4f),
                        start = Offset(0f, hitLineY),
                        end = Offset(width, hitLineY),
                        strokeWidth = 3f
                    )
                    // Blur glow
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, beamColor.copy(alpha = 0.2f), Color.Transparent),
                            startY = hitLineY - 15f,
                            endY = hitLineY + 15f
                        ),
                        topLeft = Offset(0f, hitLineY - 15f),
                        size = Size(width, 30f)
                    )

                    // 3. Draw fallback physical targets (Letters DFJK inside translucent rings)
                    for (i in 0..3) {
                        val ringX = i * laneWidth + (laneWidth / 2f)
                        
                        // Rings at landing zone
                        drawCircle(
                            color = if (i == 0 || i == 3) colors.accentLavender.copy(alpha = 0.35f) else colors.accentPink.copy(alpha = 0.35f),
                            radius = 38f,
                            center = Offset(ringX, hitLineY),
                            style = Stroke(width = 3.5f)
                        )
                    }

                    // 4. Draw descending Notes (EXTRA THICK & POPPING SIZES FOR POLISH)
                    // Render any notes whose hitTimeMs falls inside active window
                    viewModel.pcmNotes.forEach { note ->
                        if (note.judgment == "MISS") return@forEach

                        val headTime = note.hitTimeMs
                        val tailTime = note.hitTimeMs + note.holdDurationMs

                        // Skip rendering if note is hit/completed and has no remaining hold tail
                        if (note.isHit && (note.holdDurationMs <= 0 || songTime > tailTime)) {
                            return@forEach
                        }

                        val laneX = note.lane * laneWidth
                        val laneCenter = laneX + (laneWidth / 2f)

                        // 4a. Draw HOLD TAIL if applicable
                        if (note.holdDurationMs > 0) {
                            val headY = hitLineY + ((songTime - headTime).toFloat() / spawnTimeMs * hitLineY)
                            val tailY = hitLineY + ((songTime - tailTime).toFloat() / spawnTimeMs * hitLineY)

                            // Define the bounds of the tail ribbon to render
                            val bottomY = if (note.isHit) hitLineY else headY
                            val topY = tailY

                            // Only render if in visible vertical range of the field
                            val drawBottom = bottomY.coerceIn(0f, height)
                            val drawTop = topY.coerceIn(0f, height)

                            if (drawBottom > drawTop) {
                                val tailWidth = laneWidth * 0.45f
                                val isHeldActive = note.isHit && !note.isHoldBroken
                                val tailAlpha = if (isHeldActive) 0.65f else 0.35f
                                val tailColor = when (note.lane) {
                                    0 -> colors.accentLavender
                                    1 -> colors.accentCyan
                                    2 -> colors.accentPink
                                    else -> colors.accentGreen
                                }

                                // Ribbon background Capsule
                                drawRoundRect(
                                    color = tailColor.copy(alpha = tailAlpha),
                                    topLeft = Offset(laneCenter - tailWidth / 2f, drawTop),
                                    size = Size(tailWidth, drawBottom - drawTop),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )

                                // Reflective neon electricity style core line
                                drawLine(
                                    color = Color.White.copy(alpha = if (isHeldActive) 0.9f else 0.4f),
                                    start = Offset(laneCenter, drawTop),
                                    end = Offset(laneCenter, drawBottom),
                                    strokeWidth = 5.0f
                                )
                            }
                        }

                        // 4b. Draw NOTE HEAD if NOT hit yet
                        if (!note.isHit) {
                            val progress = (songTime - (headTime - spawnTimeMs)).toFloat() / spawnTimeMs
                            val noteY = progress * hitLineY

                            // Draw only if progress is within visible boundaries
                            if (progress in 0.0f..1.12f) {
                                val xPadding = 4f // Thin padding = maximum note thickness!
                                val noteH = 75f   // Extreme BOLD depth & volume!
                                val noteColor = when (note.lane) {
                                    0 -> colors.accentLavender
                                    1 -> colors.accentCyan
                                    2 -> colors.accentPink
                                    else -> colors.accentGreen
                                }

                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            noteColor.copy(alpha = 0.7f),
                                            noteColor,
                                            noteColor.copy(alpha = 0.7f)
                                        )
                                    ),
                                    topLeft = Offset(laneX + xPadding, noteY - noteH / 2f),
                                    size = Size(laneWidth - (xPadding * 2), noteH),
                                    cornerRadius = CornerRadius(20f, 20f)
                                )

                                // Bright neon reflective white core line (Highly visible thick core)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.95f),
                                    start = Offset(laneX + xPadding + 10f, noteY),
                                    end = Offset(laneX + laneWidth - xPadding - 10f, noteY),
                                    strokeWidth = 9.0f // Thick 9f neon glow core!
                                )
                            }
                        }
                    }
                }

                // 5. Hit Lane tap flash animation layer
                Row(modifier = Modifier.fillMaxSize()) {
                    for (i in 0..3) {
                        val lastTap = viewModel.laneFlashTime[i]
                        val diff = songTime - lastTap
                        val rawAlpha = (1f - (diff.toFloat() / 200f)).coerceIn(0f, 1f)
                        val alpha = if (viewModel.lanePressed.getOrNull(i) == true) 1.0f else rawAlpha

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            when (i) {
                                                0 -> colors.accentLavender.copy(alpha = alpha * 0.25f)
                                                1 -> colors.accentCyan.copy(alpha = alpha * 0.25f)
                                                2 -> colors.accentPink.copy(alpha = alpha * 0.25f)
                                                else -> colors.accentGreen.copy(alpha = alpha * 0.25f)
                                            }
                                        ),
                                        startY = 100f
                                    )
                                )
                        )
                    }
                }

                // 6. Floating feedback popups (Perfect / Great / Miss / etc.)
                viewModel.floatingFeedbacks.forEach { feedback ->
                    val localizedText = when (feedback.text.uppercase()) {
                        "PERFECT" -> TranslationManager.getString("perfect", currentLanguage)
                        "GREAT" -> TranslationManager.getString("great", currentLanguage)
                        "GOOD" -> TranslationManager.getString("good", currentLanguage)
                        "MISS" -> TranslationManager.getString("miss", currentLanguage)
                        else -> feedback.text
                    }
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val perLaneWidth = maxWidth / 4
                        Box(
                            modifier = Modifier
                                .width(perLaneWidth)
                                .align(Alignment.BottomStart)
                                .offset(x = perLaneWidth * feedback.lane)
                                .padding(bottom = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = localizedText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(android.graphics.Color.parseColor(feedback.color)),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 7. Combo overlay in middle of playing screen
                if (combo > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(bottom = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$combo",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "COMBO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = colors.lightAccentText
                        )
                    }
                }

                // 8. Invisible Lane Touch Receptors for flexible mobile tapping anywhere on lanes
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            viewModel.setLanePressed(i, true)
                                            viewModel.registerLaneTap(i)
                                            try {
                                                awaitRelease()
                                            } finally {
                                                viewModel.setLanePressed(i, false)
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            // 9. Bottom tactile controls deck keys layout (Touch target sizes >= 48dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBg, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 0..3) {
                        val keyTag = when (i) {
                            0 -> "D"
                            1 -> "F"
                            2 -> "J"
                            else -> "K"
                        }
                        
                        val lastTap = viewModel.laneFlashTime[i]
                        val diff = songTime - lastTap
                        val isActive = (diff < 150) || (viewModel.lanePressed.getOrNull(i) == true)
                        
                        val keyBgColor = if (isActive) {
                            when (i) {
                                0 -> colors.accentLavender
                                1 -> colors.accentCyan
                                2 -> colors.accentPink
                                else -> colors.accentLavender
                            }
                        } else {
                            colors.keyPassiveBg
                        }

                        val keyTextColor = if (isActive) {
                            Color(0xFF131118)
                        } else {
                            colors.textLight
                        }

                        val borderModifier = if (i == 2 && !isActive) {
                            Modifier.border(2.dp, colors.accentLavender.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.3f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(keyBgColor)
                                .then(borderModifier)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            viewModel.setLanePressed(i, true)
                                            viewModel.registerLaneTap(i)
                                            try {
                                                awaitRelease()
                                            } finally {
                                                viewModel.setLanePressed(i, false)
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keyTag,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = keyTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.accentGreen)
                        )
                        Text(
                            text = "STABLE 60FPS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = colors.lightAccentText
                        )
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.exitToTitle()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.keyPassiveBg,
                            contentColor = colors.accentLavender
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = TranslationManager.getString("cancel", currentLanguage).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HUDPanel(
    songName: String,
    difficulty: String,
    score: Int,
    accuracy: Float,
    health: Float,
    songProgress: Float,
    colors: ThemeColors,
    currentLanguage: AppLanguage,
    onPauseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.cardBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = colors.accentLavender
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = songName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%07d".format(score),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Acc: %.1f%%".format(accuracy),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.lightAccentText
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.lightAccentText
                    )
                    Text(
                        text = "VITALITY: ${(health * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.accentLavender
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.keyPassiveBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(health)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(colors.accentLavender, colors.accentCyan)
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { songProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = colors.accentLavender,
            trackColor = Color.Transparent,
        )
    }
}

/**
 * --- 3. RESULTS SUCCESS SCREEN ---
 */
@Composable
fun ResultsScreen(viewModel: GameViewModel) {
    val score by viewModel.score.collectAsStateWithLifecycle()
    val maxCombo by viewModel.maxCombo.collectAsStateWithLifecycle()
    val accuracy by viewModel.accuracy.collectAsStateWithLifecycle()
    val perfects by viewModel.perfectCount.collectAsStateWithLifecycle()
    val greats by viewModel.greatCount.collectAsStateWithLifecycle()
    val goods by viewModel.goodCount.collectAsStateWithLifecycle()
    val misses by viewModel.missCount.collectAsStateWithLifecycle()
    val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val activeTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

    val colors = ThemeManager.getColors(activeTheme)
    val rank = viewModel.calculateFinalRank()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = colors.bg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = TranslationManager.getString("results", currentLanguage),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    ),
                    color = colors.accentLavender
                )
                Text(
                    text = selectedSong.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textLight
                )
            }

            // Rank Badge Circle
            val rankGlowColor = when (rank) {
                "S" -> colors.accentLavender
                "A" -> colors.accentCyan
                "B" -> colors.accentGreen
                "C" -> Color(0xFFFFCC80)
                else -> colors.accentPink
            }
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(colors.cardBg)
                    .border(2.dp, rankGlowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = rankGlowColor
                )
            }

            // Stat Metrics Board
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.cardBg)
                    .border(1.dp, colors.keyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultMetricRow(TranslationManager.getString("total_score", currentLanguage), "$score pts", colors.accentCyan, colors)
                ResultMetricRow(TranslationManager.getString("accuracy", currentLanguage), "%.2f%%".format(accuracy), colors.accentLavender, colors)
                ResultMetricRow(TranslationManager.getString("max_combo", currentLanguage), "x$maxCombo Hits", Color.White, colors)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.keyPassiveBg.copy(alpha = 0.3f))
                )

                ResultMetricRow(TranslationManager.getString("perfects", currentLanguage), "$perfects", colors.accentLavender, colors)
                ResultMetricRow(TranslationManager.getString("greats", currentLanguage), "$greats", colors.accentCyan, colors)
                ResultMetricRow(TranslationManager.getString("goods", currentLanguage), "$goods", Color(0xFFFFCC80), colors)
                ResultMetricRow(TranslationManager.getString("misses", currentLanguage), "$misses", colors.accentPink, colors)
            }

            // Actions Buttons Row (Touch targets >= 48dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.exitToTitle()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    border = BorderStroke(1.dp, colors.keyPassiveBg),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = TranslationManager.getString("exit", currentLanguage), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.startGame()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("replay_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentLavender, contentColor = colors.bg),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = TranslationManager.getString("retry", currentLanguage), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun ResultMetricRow(label: String, value: String, valueColor: Color, colors: ThemeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = colors.lightAccentText)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = valueColor
        )
    }
}

/**
 * --- 4. GAME FAILED SCREEN ---
 */
@Composable
fun FailedScreen(viewModel: GameViewModel) {
    val score by viewModel.score.collectAsStateWithLifecycle()
    val maxCombo by viewModel.maxCombo.collectAsStateWithLifecycle()
    val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val activeTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    
    val colors = ThemeManager.getColors(activeTheme)
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = colors.bg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.accentPink,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = TranslationManager.getString("failed", currentLanguage),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    ),
                    color = colors.accentPink
                )
                Text(
                    text = "VITALITY DELETED",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    color = colors.lightAccentText
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.cardBg)
                    .border(1.dp, colors.keyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedSong.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val difficultyLabel = when (selectedSong.difficulty) {
                    "Easy" -> TranslationManager.getString("easy", currentLanguage)
                    "Normal" -> TranslationManager.getString("normal", currentLanguage)
                    else -> TranslationManager.getString("hard", currentLanguage)
                }

                Text(
                    text = "${TranslationManager.getString("select_diff", currentLanguage)}: ${difficultyLabel.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accentPink
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.keyPassiveBg.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = TranslationManager.getString("total_score", currentLanguage), style = MaterialTheme.typography.bodySmall, color = colors.lightAccentText)
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = colors.accentCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = TranslationManager.getString("max_combo", currentLanguage), style = MaterialTheme.typography.bodySmall, color = colors.lightAccentText)
                        Text(
                            text = "x$maxCombo",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Touch Targets >= 48dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.exitToTitle()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    border = BorderStroke(1.dp, colors.keyPassiveBg),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = TranslationManager.getString("exit", currentLanguage), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.startGame()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("retry_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPink, contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = TranslationManager.getString("retry", currentLanguage), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
