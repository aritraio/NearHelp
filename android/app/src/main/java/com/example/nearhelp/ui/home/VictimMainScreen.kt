package com.example.nearhelp.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab

/**
 * Unified container for victim bottom navigation tabs (Home, Chat, Map, Profile).
 * Provides fluid, directional Pop-In / Pop-Out page transitions between tabs with spatial awareness
 * while preserving scroll states and keeping ViewModels cached.
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
    var isHomeOverlayActive by remember { mutableStateOf(false) }
    val saveableStateHolder = rememberSaveableStateHolder()

    LaunchedEffect(initialTab) {
        currentTab = initialTab
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
            AnimatedVisibility(
                visible = isBottomNavVisible,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(140)),
            ) {
                VictimBottomNavBar(
                    selected = currentTab,
                    onSelect = { tab ->
                        currentTab = tab
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
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val isForward = targetState.ordinal > initialState.ordinal
                    if (isForward) {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            initialOffsetX = { (it * 0.16f).toInt() }
                        ) + scaleIn(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            initialScale = 0.94f
                        ) + fadeIn(
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        )) togetherWith (
                        slideOutHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            targetOffsetX = { -(it * 0.16f).toInt() }
                        ) + scaleOut(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            targetScale = 0.96f
                        ) + fadeOut(
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ))
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            initialOffsetX = { -(it * 0.16f).toInt() }
                        ) + scaleIn(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            initialScale = 0.94f
                        ) + fadeIn(
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        )) togetherWith (
                        slideOutHorizontally(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            targetOffsetX = { (it * 0.16f).toInt() }
                        ) + scaleOut(
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                            targetScale = 0.96f
                        ) + fadeOut(
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ))
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
                                onNavigateToAssistant = {
                                    currentTab = VictimNavTab.CHAT
                                },
                                viewModel = authViewModel,
                                showBottomBar = false,
                                onOverlayStateChanged = { isHomeOverlayActive = it },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        VictimNavTab.CHAT -> {
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
