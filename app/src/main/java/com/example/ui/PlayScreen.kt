package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.foundation.BorderStroke
import com.example.data.ScoreRecord
import com.example.game.GameNote
import com.example.game.GameViewModel
import com.example.game.Song
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Style Constants
val DeepSpaceBg = Color(0xFF1D1B20)
val SlateCardBg = Color(0xFF2B2930)
val AccentCyan = Color(0xFFB3E5FC)
val AccentPink = Color(0xFFF48FB1)
val AccentLavender = Color(0xFFD0BCFE)
val AccentGreen = Color(0xFFA5D6A7)
val LightAccentText = Color(0xFFCCC2DC)
val TextLight = Color(0xFFE6E1E5)
val KeyPassiveBg = Color(0xFF49454F)

@Composable
fun MainRhythmApp(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DeepSpaceBg
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
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Trigger initial observing hook
    LaunchedEffect(Unit) {
        viewModel.observeHighScoreForSelectedSong()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DeepSpaceBg
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
                color = TextLight,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "CHIPTUNE SYNTH EDITION",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = AccentLavender,
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
                // Section 1: Song Selection
                item {
                    Text(
                        text = "SELECT SONG",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = TextLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display Preset Cards
                    viewModel.songs.forEach { song ->
                        SongCard(
                            song = song,
                            isSelected = song.name == selectedSong.name,
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
                        text = "BEST RECORD",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = TextLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SlateCardBg)
                            .border(1.dp, KeyPassiveBg.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        if (highScoreRecord != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "High Score: ${highScoreRecord!!.score}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = AccentCyan
                                    )
                                    Text(
                                        text = "Max Combo: x${highScoreRecord!!.maxCombo}  |  Acc: %.1f%%".format(highScoreRecord!!.accuracy),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LightAccentText
                                    )
                                    Text(
                                        text = "Perfects: ${highScoreRecord!!.perfectCount}  |  Misses: ${highScoreRecord!!.missCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightAccentText.copy(alpha = 0.8f)
                                    )
                                }

                                // Large glowing rank letter badge
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(KeyPassiveBg)
                                        .border(2.dp, AccentPink, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = highScoreRecord!!.rank,
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
                                    tint = LightAccentText,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No beatmap score records found for this track.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LightAccentText,
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
                            text = "RECENT PLAY HISTORIES",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = TextLight
                        )
                        
                        if (historyList.isNotEmpty()) {
                            Text(
                                text = "CLEAR LOGS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPink
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
                                .background(SlateCardBg)
                                .border(1.dp, KeyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent records. Play a song to save a historical entry!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightAccentText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(historyList) { record ->
                        HistoryRow(record)
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
                    containerColor = AccentLavender,
                    contentColor = Color(0xFF381E72)
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
                        text = "START PLAYING",
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
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentLavender else KeyPassiveBg.copy(alpha = 0.3f)
    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(SlateCardBg, Color(0xFF381E72).copy(alpha = 0.15f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(SlateCardBg, SlateCardBg)
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
                    .background(if (isSelected) KeyPassiveBg else DeepSpaceBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isSelected) AccentLavender else LightAccentText
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
                    color = TextLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "BPM ${song.bpm}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightAccentText
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightAccentText
                    )
                    Text(
                        text = "Length 1:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightAccentText
                    )
                }
            }

            // Difficulty Badge Toggle
            val diffColor = when (song.difficulty) {
                "Easy" -> AccentGreen
                "Normal" -> Color(0xFFFFAA00)
                else -> AccentPink
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(diffColor.copy(alpha = 0.15f))
                    .border(1.dp, diffColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = song.difficulty.uppercase(),
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
fun HistoryRow(record: ScoreRecord) {
    val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val dateStr = fmt.format(Date(record.timestamp))

    val diffColor = when (record.difficulty) {
        "Easy" -> AccentGreen
        "Normal" -> Color(0xFFFFAA00)
        else -> AccentPink
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardBg.copy(alpha = 0.6f))
            .border(1.dp, KeyPassiveBg.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = record.songName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextLight
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = record.difficulty.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = diffColor
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightAccentText.copy(alpha = 0.3f)
                )
                Text(
                    text = "x${record.maxCombo} Combo",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightAccentText
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightAccentText.copy(alpha = 0.3f)
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightAccentText.copy(alpha = 0.6f)
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
                color = AccentCyan
            )
            Text(
                text = "GRADE ${record.rank}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                ),
                color = AccentPink
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

    val haptic = LocalHapticFeedback.current

    // Set constant target hit windows
    val spawnTimeMs = 1200L

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepSpaceBg
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
                    val hitLineY = height * 0.85f

                    // 1. Draw Lane board backgrounds
                    for (i in 0..3) {
                        val startX = i * laneWidth
                        
                        // Lane strip background
                        drawRect(
                            color = if (i % 2 == 0) SlateCardBg.copy(alpha = 0.35f) else SlateCardBg.copy(alpha = 0.2f),
                            topLeft = Offset(startX, 0f),
                            size = Size(laneWidth, height)
                        )

                        // Outer separator borders
                        if (i > 0) {
                            drawLine(
                                color = KeyPassiveBg.copy(alpha = 0.3f),
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

                    // 3. Draw fallback physical keys static targets (Letters DFJK inside translucent rings)
                    for (i in 0..3) {
                        val ringX = i * laneWidth + (laneWidth / 2f)
                        
                        // Rings at landing zone
                        drawCircle(
                            color = if (i == 0 || i == 3) AccentLavender.copy(alpha = 0.3f) else AccentPink.copy(alpha = 0.3f),
                            radius = 36f,
                            center = Offset(ringX, hitLineY),
                            style = Stroke(width = 2f)
                        )
                    }

                    // 4. Draw descending Notes
                    // Render any notes whose hitTimeMs falls inside active window
                    viewModel.pcmNotes.forEach { note ->
                        if (note.isHit || note.judgment == "MISS") return@forEach

                        val progress = (songTime - (note.hitTimeMs - spawnTimeMs)).toFloat() / spawnTimeMs
                        val noteY = progress * hitLineY

                        // Note is visible before collision and shortly after
                        if (progress in 0.0f..1.12f) {
                            val laneX = note.lane * laneWidth
                            val xPadding = 12f
                            val noteH = 26f
                            val noteColor = when (note.lane) {
                                0 -> AccentLavender
                                1 -> AccentCyan
                                2 -> AccentPink
                                else -> AccentLavender
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
                                cornerRadius = CornerRadius(12f, 12f)
                            )

                            // Bright neon reflective white core line
                            drawLine(
                                color = Color.White.copy(alpha = 0.9f),
                                start = Offset(laneX + xPadding + 8f, noteY),
                                end = Offset(laneX + laneWidth - xPadding - 8f, noteY),
                                strokeWidth = 3f
                            )
                        }
                    }
                }

                // 5. Hit Lane tap flash animation layer
                Row(modifier = Modifier.fillMaxSize()) {
                    for (i in 0..3) {
                        val lastTap = viewModel.laneFlashTime[i]
                        val diff = songTime - lastTap
                        val alpha = (1f - (diff.toFloat() / 200f)).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            when (i) {
                                                0 -> AccentLavender.copy(alpha = alpha * 0.25f)
                                                1 -> AccentCyan.copy(alpha = alpha * 0.25f)
                                                2 -> AccentPink.copy(alpha = alpha * 0.25f)
                                                else -> AccentLavender.copy(alpha = alpha * 0.25f)
                                            }
                                        ),
                                        startY = 100f
                                    )
                                )
                        )
                    }
                }

                // 6. Floating Judgments Text & Combo overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (combo > 2) {
                        Text(
                            text = "$combo",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "COMBO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 4.sp
                            ),
                            color = AccentLavender
                        )
                    }
                }

                // Rising Floating Judgment Feedbacks
                viewModel.floatingFeedbacks.forEach { feedback ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .align(Alignment.BottomStart)
                                .offset(
                                    x = (feedback.lane * 90).dp
                                )
                                .padding(bottom = 120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = feedback.text,
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

                // Invisible Lane Touch Receptors for flexible mobile tapping anywhere on lanes
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
                                            viewModel.registerLaneTap(i)
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            // 3. Bottom persistent controls deck (New design addition matching user's spec exactly)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateCardBg, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
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
                         val isActive = diff < 150
                         
                         val keyBgColor = if (isActive) {
                             when (i) {
                                 0 -> AccentLavender
                                 1 -> AccentCyan
                                 2 -> AccentPink
                                 else -> AccentLavender
                             }
                         } else {
                             KeyPassiveBg
                         }

                         val keyTextColor = if (isActive) {
                             when (i) {
                                 0 -> Color(0xFF381E72)
                                 1 -> Color(0xFF01579B)
                                 2 -> Color(0xFF381E72)
                                 else -> Color(0xFF381E72)
                             }
                         } else {
                             Color.White
                         }

                         val borderModifier = if (i == 2 && !isActive) {
                             Modifier.border(2.dp, AccentLavender.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                         } else Modifier

                         Box(
                             modifier = Modifier
                                 .weight(1f)
                                 .aspectRatio(1f)
                                 .clip(RoundedCornerShape(20.dp))
                                 .background(keyBgColor)
                                 .then(borderModifier)
                                 .pointerInput(Unit) {
                                     detectTapGestures(
                                         onPress = {
                                             viewModel.registerLaneTap(i)
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
                                 .background(Color(0xFF4CAF50))
                         )
                         Text(
                             text = "STABLE 60FPS",
                             style = MaterialTheme.typography.labelSmall.copy(
                                 fontSize = 10.sp,
                                 fontWeight = FontWeight.Bold,
                                 letterSpacing = 0.5.sp
                             ),
                             color = LightAccentText
                         )
                     }

                     Button(
                         onClick = {
                             haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                             viewModel.exitToTitle()
                         },
                         colors = ButtonDefaults.buttonColors(
                             containerColor = Color(0xFF381E72),
                             contentColor = AccentLavender
                         ),
                         shape = RoundedCornerShape(50),
                         contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                         modifier = Modifier.height(34.dp)
                     ) {
                         Text(
                             text = "PAUSE",
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
    onPauseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateCardBg)
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
                    color = AccentLavender
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
                        color = LightAccentText
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = LightAccentText
                    )
                    Text(
                        text = "VITALITY: ${(health * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentLavender
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
                    .background(KeyPassiveBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(health)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AccentLavender, AccentCyan)
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
            color = AccentLavender,
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

    val rank = viewModel.calculateFinalRank()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DeepSpaceBg
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
                    text = "STAGE CLEAR",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    ),
                    color = AccentLavender
                )
                Text(
                    text = selectedSong.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextLight
                )
            }

            // Rank Badge Circle
            val rankGlowColor = when (rank) {
                "S" -> AccentLavender
                "A" -> AccentCyan
                "B" -> Color(0xFFC5E1A5)
                "C" -> Color(0xFFFFCC80)
                else -> AccentPink
            }
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(SlateCardBg)
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
                    .background(SlateCardBg)
                    .border(1.dp, KeyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultMetricRow("Total Score", "$score pts", AccentCyan)
                ResultMetricRow("Accuracy", "%.2f%%".format(accuracy), AccentLavender)
                ResultMetricRow("Max Combo", "x$maxCombo Hits", Color.White)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(KeyPassiveBg.copy(alpha = 0.3f))
                )

                ResultMetricRow("Perfect", "$perfects", AccentLavender)
                ResultMetricRow("Great", "$greats", AccentCyan)
                ResultMetricRow("Good", "$goods", Color(0xFFFFCC80))
                ResultMetricRow("Miss", "$misses", AccentPink)
            }

            // Actions Buttons Row
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
                    border = BorderStroke(1.dp, KeyPassiveBg),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "BACK TO TITLE", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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
                    colors = ButtonDefaults.buttonColors(containerColor = AccentLavender, contentColor = Color(0xFF381E72)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "REPLAY SONG", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun ResultMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = LightAccentText)
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
    
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DeepSpaceBg
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
                    tint = AccentPink,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "VITALITY DELETED",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp
                    ),
                    color = AccentPink
                )
                Text(
                    text = "STAGES ATTEMPT FAILED",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    color = LightAccentText
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SlateCardBg)
                    .border(1.dp, KeyPassiveBg.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedSong.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Difficulty: ${selectedSong.difficulty.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentPink
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(KeyPassiveBg.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Final Score", style = MaterialTheme.typography.bodySmall, color = LightAccentText)
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = AccentCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Max Combo", style = MaterialTheme.typography.bodySmall, color = LightAccentText)
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
                    border = BorderStroke(1.dp, KeyPassiveBg),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "EXIT MENU", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink, contentColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = "TRY AGAIN", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
