package com.example.nearhelp

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.util.UUID
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nearhelp.theme.NearHelpTheme
import com.example.nearhelp.theme.ThemeManager

class MainActivity : ComponentActivity() {
  private var pendingIncident by mutableStateOf<String?>(null)
  private fun readIncident(intent: Intent?) {
    pendingIncident = intent?.getStringExtra("incident_id")?.takeIf { runCatching { UUID.fromString(it) }.isSuccess }
  }
  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    readIncident(intent)
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    readIncident(intent)
    enableEdgeToEdge()
    setContent {
      NearHelpTheme(darkTheme = ThemeManager.isDarkMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainNavigation(pendingIncidentId = pendingIncident, onIncidentConsumed = { pendingIncident = null; intent.removeExtra("incident_id") })
        }
      }
    }
  }
}
