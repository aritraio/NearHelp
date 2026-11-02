package com.example.nearhelp.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.nearhelp.ui.navigation.AppMotion
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab

/**
 * Unified container for victim bottom navigation tabs (Home, Chat, Map, Profile).
 * Provides subtle directional transitions and a stable content viewport
 * while preserving scroll states and keeping ViewModels cached.
 */
@Composable
fun VictimMainScreen(
    initialTab: VictimNavTab = VictimNavTab.HOME,
    conditionId: String = "cardiac_arrest",
    sessionId: String = "KOL-SOS-8821",
    onNavigateToLogin: () -> Unit,
    onNavigateToTracking: () -> Unit = {},
    onCreateSos: () -> Unit = onNavigateToTracking,
    onNavigateToNavigation: () -> Unit = {},
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    var currentTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    val saveableStateHolder = rememberSaveableStateHolder()

    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(NearHelpApplication.instance.userRepository)
    }

    // Return to Home tab on back press before exiting/popping
    BackHandler(enabled = currentTab != VictimNavTab.HOME) {
        currentTab = VictimNavTab.HOME
    }

    // The shared bar stays mounted so tab changes never resize the content viewport.
    Scaffold(
        bottomBar = {
            VictimBottomNavBar(
                selected = currentTab,
                onSelect = { tab -> if (tab != currentTab) currentTab = tab },
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    // Avoid translating the native map surface during tab changes.
                    if (initialState == VictimNavTab.MAP || targetState == VictimNavTab.MAP) {
                        AppMotion.fade()
                    } else {
                        AppMotion.tab(forward = targetState.ordinal > initialState.ordinal)
                    }
                },
                label = "victim_main_tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                saveableStateHolder.SaveableStateProvider(key = tab) {
                    when (tab) {
                        VictimNavTab.HOME -> {
                            HomeScreen(
                                onNavigateToLogin = onNavigateToLogin,
                                onNavigateToProfile = {
                                    currentTab = VictimNavTab.PROFILE
                                },
                                onNavigateToResponderProfile = {
                                    currentTab = VictimNavTab.PROFILE
                                    profileViewModel.openQualificationsDialog()
                                },
                                onNavigateToHistory = {
                                    currentTab = VictimNavTab.PROFILE
                                    profileViewModel.openEmergencyHistoryDialog()
                                },
                                onNavigateToMap = {
                                    currentTab = VictimNavTab.MAP
                                },
                                onNavigateToTracking = onNavigateToTracking,
                                onCreateSos = onCreateSos,
                                onNavigateToAssistant = {
                                    currentTab = VictimNavTab.CHAT
                                },
                                viewModel = authViewModel,
                                showBottomBar = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        VictimNavTab.CHAT -> {
                            val assistantViewModel: AiCrisisAssistantViewModel = viewModel {
                                AiCrisisAssistantViewModel(NearHelpApplication.instance.aiAgentRepository)
                            }
                            AiCrisisAssistantScreen(
                                onNavigateBack = { currentTab = VictimNavTab.HOME },
                                onNavigateToHome = { currentTab = VictimNavTab.HOME },
                                onNavigateToMap = {
                                    currentTab = VictimNavTab.MAP
                                },
                                onNavigateToProfile = {
                                    currentTab = VictimNavTab.PROFILE
                                },
                                viewModel = assistantViewModel,
                                conditionId = conditionId,
                                sessionId = sessionId,
                                showBottomBar = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        VictimNavTab.MAP -> {
                            val mapViewModel: CommunityGeoMapViewModel = viewModel()
                            CommunityGeoMapScreen(
                                onNavigateBack = { currentTab = VictimNavTab.HOME },
                                onNavigateToHome = { currentTab = VictimNavTab.HOME },
                                onNavigateToAssistant = {
                                    currentTab = VictimNavTab.CHAT
                                },
                                onNavigateToProfile = {
                                    currentTab = VictimNavTab.PROFILE
                                },
                                onNavigateToTracking = onNavigateToTracking,
                                onNavigateToNavigation = onNavigateToNavigation,
                                viewModel = mapViewModel,
                                showBottomBar = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        VictimNavTab.PROFILE -> {
                            ProfileScreen(
                                onNavigateBack = { currentTab = VictimNavTab.HOME },
                                onNavigateToHome = { currentTab = VictimNavTab.HOME },
                                onNavigateToAssistant = {
                                    currentTab = VictimNavTab.CHAT
                                },
                                onNavigateToMap = {
                                    currentTab = VictimNavTab.MAP
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
    }
}
