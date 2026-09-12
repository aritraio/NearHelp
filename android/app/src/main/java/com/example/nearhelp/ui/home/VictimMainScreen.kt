package com.example.nearhelp.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.ui.assistant.AiCrisisAssistantScreen
import com.example.nearhelp.ui.assistant.AiCrisisAssistantViewModel
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.map.CommunityGeoMapScreen
import com.example.nearhelp.ui.map.CommunityGeoMapViewModel
import com.example.nearhelp.ui.profile.ProfileScreen
import com.example.nearhelp.ui.profile.ProfileViewModel
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab

/**
 * High-performance tab layout helper that keeps visited composables alive in memory
 * and immediately places the active tab without recomposition, jank, or double-measure overhead.
 */
private fun Modifier.tabVisibility(visible: Boolean): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        if (visible) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Unified container for victim bottom navigation tabs (Home, Chat, Map, Profile).
 * Keeps the bottom navigation bar stationary and persistent so it never slides or flickers,
 * while switching tabs with zero latency (instant 0ms) and keeping UI state/scroll preserved across tabs.
 */
@Composable
fun VictimMainScreen(
    initialTab: VictimNavTab = VictimNavTab.HOME,
    conditionId: String = "cardiac_arrest",
    sessionId: String = "KOL-SOS-8821",
    onNavigateToLogin: () -> Unit,
    onNavigateToTracking: () -> Unit = {},
    onNavigateToNavigation: () -> Unit = {},
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    var currentTab by rememberSaveable { mutableStateOf(initialTab) }
    var visitedTabs by rememberSaveable { mutableStateOf(setOf(initialTab.name)) }
    var isHomeOverlayActive by remember { mutableStateOf(false) }

    LaunchedEffect(initialTab) {
        currentTab = initialTab
        visitedTabs = visitedTabs + initialTab.name
    }

    // Persist ViewModels across tab switches to avoid re-fetching and jank
    val assistantViewModel: AiCrisisAssistantViewModel = viewModel {
        AiCrisisAssistantViewModel(NearHelpApplication.instance.aiAgentRepository)
    }
    val mapViewModel: CommunityGeoMapViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(NearHelpApplication.instance.userRepository)
    }

    // Return to Home tab on back press before exiting/popping
    BackHandler(enabled = currentTab != VictimNavTab.HOME) {
        currentTab = VictimNavTab.HOME
    }

    val isBottomNavVisible = currentTab != VictimNavTab.HOME || !isHomeOverlayActive

    Scaffold(
        bottomBar = {
            if (isBottomNavVisible) {
                VictimBottomNavBar(
                    selected = currentTab,
                    onSelect = { tab ->
                        currentTab = tab
                        visitedTabs = visitedTabs + tab.name
                    }
                )
            }
        },
        containerColor = VictimBackground,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Tab 1: HOME
            if (VictimNavTab.HOME.name in visitedTabs) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .tabVisibility(currentTab == VictimNavTab.HOME)
                ) {
                    HomeScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToProfile = {
                            currentTab = VictimNavTab.PROFILE
                            visitedTabs = visitedTabs + VictimNavTab.PROFILE.name
                        },
                        onNavigateToMap = {
                            currentTab = VictimNavTab.MAP
                            visitedTabs = visitedTabs + VictimNavTab.MAP.name
                        },
                        onNavigateToTracking = onNavigateToTracking,
                        onNavigateToAssistant = {
                            currentTab = VictimNavTab.CHAT
                            visitedTabs = visitedTabs + VictimNavTab.CHAT.name
                        },
                        viewModel = authViewModel,
                        showBottomBar = false,
                        onOverlayStateChanged = { isHomeOverlayActive = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Tab 2: CHAT
            if (VictimNavTab.CHAT.name in visitedTabs) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .tabVisibility(currentTab == VictimNavTab.CHAT)
                ) {
                    AiCrisisAssistantScreen(
                        onNavigateBack = { currentTab = VictimNavTab.HOME },
                        onNavigateToHome = { currentTab = VictimNavTab.HOME },
                        onNavigateToMap = {
                            currentTab = VictimNavTab.MAP
                            visitedTabs = visitedTabs + VictimNavTab.MAP.name
                        },
                        onNavigateToProfile = {
                            currentTab = VictimNavTab.PROFILE
                            visitedTabs = visitedTabs + VictimNavTab.PROFILE.name
                        },
                        viewModel = assistantViewModel,
                        conditionId = conditionId,
                        sessionId = sessionId,
                        showBottomBar = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Tab 3: MAP
            if (VictimNavTab.MAP.name in visitedTabs) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .tabVisibility(currentTab == VictimNavTab.MAP)
                ) {
                    CommunityGeoMapScreen(
                        onNavigateBack = { currentTab = VictimNavTab.HOME },
                        onNavigateToHome = { currentTab = VictimNavTab.HOME },
                        onNavigateToAssistant = {
                            currentTab = VictimNavTab.CHAT
                            visitedTabs = visitedTabs + VictimNavTab.CHAT.name
                        },
                        onNavigateToProfile = {
                            currentTab = VictimNavTab.PROFILE
                            visitedTabs = visitedTabs + VictimNavTab.PROFILE.name
                        },
                        onNavigateToTracking = onNavigateToTracking,
                        onNavigateToNavigation = onNavigateToNavigation,
                        viewModel = mapViewModel,
                        showBottomBar = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Tab 4: PROFILE
            if (VictimNavTab.PROFILE.name in visitedTabs) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .tabVisibility(currentTab == VictimNavTab.PROFILE)
                ) {
                    ProfileScreen(
                        onNavigateBack = { currentTab = VictimNavTab.HOME },
                        onNavigateToHome = { currentTab = VictimNavTab.HOME },
                        onNavigateToAssistant = {
                            currentTab = VictimNavTab.CHAT
                            visitedTabs = visitedTabs + VictimNavTab.CHAT.name
                        },
                        onNavigateToMap = {
                            currentTab = VictimNavTab.MAP
                            visitedTabs = visitedTabs + VictimNavTab.MAP.name
                        },
                        viewModel = profileViewModel,
                        showBottomBar = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
