package com.example.nearhelp.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.nearhelp.theme.GuardianBgGradient
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.screens.CrisisDispatchScreen
import com.example.nearhelp.ui.screens.GuardianRadarScreen

enum class HomeDisplayState {
    GUARDIAN_RADAR,
    CRISIS_DISPATCH
}

/**
 * Redesigned NearHelp Home Screen
 *
 * Seamlessly integrates:
 * 1. The Calm Guardian State (GuardianRadarScreen) with 360° radar canvas & safe index
 * 2. The High-Urgency Crisis Dispatch State (CrisisDispatchScreen) with 16-category matrix & 3s countdown
 */
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {},
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    var displayState by remember { mutableStateOf(HomeDisplayState.GUARDIAN_RADAR) }
    var selectedCrisisCategory by remember { mutableStateOf("robbery") }
    val isAnonymous = viewModel.isAnonymous()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GuardianBgGradient)
    ) {
        Crossfade(
            targetState = displayState,
            label = "HomeStateCrossfade"
        ) { state ->
            when (state) {
                HomeDisplayState.GUARDIAN_RADAR -> {
                    GuardianRadarScreen(
                        onExit = {
                            viewModel.logout()
                            onNavigateToLogin()
                        },
                        onNavigateToCrisis = { category ->
                            selectedCrisisCategory = category
                            displayState = HomeDisplayState.CRISIS_DISPATCH
                        },
                        onNavigateToMap = onNavigateToMap,
                        onNavigateToProfile = onNavigateToProfile,
                        onVoiceSosClick = onNavigateToAssistant,
                        isAnonymous = isAnonymous,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                HomeDisplayState.CRISIS_DISPATCH -> {
                    CrisisDispatchScreen(
                        initialCategory = selectedCrisisCategory,
                        onCancel = {
                            displayState = HomeDisplayState.GUARDIAN_RADAR
                        },
                        onDispatch = { category, address ->
                            // Trigger SOS and transition to Live Tracking & Rescue Navigation
                            onNavigateToTracking()
                        },
                        onNavigateToMap = onNavigateToMap,
                        onNavigateToAssistant = onNavigateToAssistant,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

