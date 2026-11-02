package com.example.nearhelp.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.design.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToHome: () -> Unit, viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { delay(700); if (viewModel.checkExistingSession()) onNavigateToHome() else onNavigateToLogin() }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
        CareBrand()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Icon(Icons.Outlined.MonitorHeart, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Help, closer to you.", style = MaterialTheme.typography.headlineLarge)
            CareMuted("Connect. Respond. Save time.")
        }
        CareMuted("Opening NearHelp…", Modifier.align(Alignment.CenterHorizontally))
    }
}
