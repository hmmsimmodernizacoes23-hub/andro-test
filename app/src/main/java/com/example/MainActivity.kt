package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.MainRhythmApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private lateinit var gameViewModel: GameViewModel

  override fun getAttributionTag(): String? {
    return "default"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        gameViewModel = viewModel()
        MainRhythmApp(viewModel = gameViewModel)
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    val lane = when (keyCode) {
      KeyEvent.KEYCODE_D -> 0
      KeyEvent.KEYCODE_F -> 1
      KeyEvent.KEYCODE_J -> 2
      KeyEvent.KEYCODE_K -> 3
      else -> -1
    }
    if (lane != -1) {
      if (::gameViewModel.isInitialized) {
        gameViewModel.registerLaneTap(lane)
      }
      return true
    }
    return super.onKeyDown(keyCode, event)
  }
}

