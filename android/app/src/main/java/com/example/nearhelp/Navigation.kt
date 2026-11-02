package com.example.nearhelp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.nearhelp.ui.incident.IncidentScreen
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import com.example.nearhelp.ui.navigation.AppMotion
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.screens.LoginScreen
import com.example.nearhelp.ui.auth.screens.PhoneOtpScreen
import com.example.nearhelp.ui.auth.screens.SignUpScreen
import com.example.nearhelp.ui.auth.screens.SplashScreen
import com.example.nearhelp.ui.home.VictimMainScreen
import com.example.nearhelp.ui.victim.VictimNavTab

private fun <T : Any> appTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
  if (initialState.key is SplashNavKey || targetState.key is SplashNavKey) {
    AppMotion.fade()
  } else {
    AppMotion.page(forward = true)
  }
}

private fun <T : Any> appPopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
  if (initialState.key is SplashNavKey || targetState.key is SplashNavKey) {
    AppMotion.fade()
  } else {
    AppMotion.page(forward = false)
  }
}

private fun <T : Any> appPredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = { swipeEdge ->
  AppMotion.page(forward = swipeEdge == NavigationEvent.EDGE_RIGHT)
}

@Composable
fun MainNavigation(
  pendingIncidentId: String? = null,
  onIncidentConsumed: () -> Unit = {},
  authViewModel: AuthViewModel = viewModel {
    AuthViewModel(NearHelpApplication.instance.authRepository)
  }
) {
  val backStack = rememberNavBackStack(SplashNavKey)
  LaunchedEffect(pendingIncidentId, backStack.lastOrNull()) {
    if (pendingIncidentId != null && backStack.lastOrNull() !in listOf(SplashNavKey, LoginNavKey, SignUpNavKey, PhoneOtpNavKey)) {
      backStack.add(LiveTrackingNavKey(pendingIncidentId))
      onIncidentConsumed()
    }
  }

  NavDisplay(
    backStack = backStack,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    onBack = { backStack.removeLastOrNull() },
    transitionSpec = appTransitionSpec(),
    popTransitionSpec = appPopTransitionSpec(),
    predictivePopTransitionSpec = appPredictivePopTransitionSpec(),
    entryProvider =
      entryProvider {
        entry<SplashNavKey> {
          SplashScreen(
            onNavigateToLogin = {
              backStack.clear()
              backStack.add(LoginNavKey)
            },
            onNavigateToHome = {
              backStack.clear()
              backStack.add(HomeNavKey)
            },
            viewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<LoginNavKey> {
          LoginScreen(
            onNavigateToSignUp = { backStack.add(SignUpNavKey) },
            onNavigateToPhoneOtp = { backStack.add(PhoneOtpNavKey) },
            onNavigateToHome = {
              backStack.clear()
              backStack.add(HomeNavKey)
            },
            viewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<SignUpNavKey> {
          SignUpScreen(
            onNavigateToLogin = { backStack.removeLastOrNull() },
            onNavigateToHome = {
              backStack.clear()
              backStack.add(HomeNavKey)
            },
            viewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<PhoneOtpNavKey> {
          PhoneOtpScreen(
            onNavigateToLogin = { backStack.removeLastOrNull() },
            onNavigateToHome = {
              backStack.clear()
              backStack.add(HomeNavKey)
            },
            viewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<GuardianRadarNavKey> {
          com.example.nearhelp.ui.screens.GuardianRadarScreen(
            onExit = { backStack.removeLastOrNull() },
            onNavigateToCrisis = { _ -> backStack.add(CrisisDispatchNavKey) },
            onNavigateToMap = { backStack.add(CommunityMapNavKey) },
            onNavigateToProfile = { backStack.add(ProfileNavKey) },
            onVoiceSosClick = { backStack.add(CrisisDispatchNavKey) },
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<CrisisDispatchNavKey> {
          IncidentScreen(createMode = true, onBack = { backStack.removeLastOrNull() })
        }
        entry<SosCreateNavKey> {
          IncidentScreen(createMode = true, onBack = { backStack.removeLastOrNull() })
        }

        entry<HomeNavKey> {
          VictimMainScreen(
            initialTab = VictimNavTab.HOME,
            onNavigateToLogin = {
              backStack.clear()
              backStack.add(LoginNavKey)
            },
            onNavigateToTracking = {
              backStack.add(LiveTrackingNavKey())
            },
            onNavigateToNavigation = {
              backStack.add(RescueNavigationNavKey())
            },
            onCreateSos = { backStack.add(SosCreateNavKey) },
            authViewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<ProfileNavKey> {
          VictimMainScreen(
            initialTab = VictimNavTab.PROFILE,
            onNavigateToLogin = {
              backStack.clear()
              backStack.add(LoginNavKey)
            },
            onNavigateToTracking = {
              backStack.add(LiveTrackingNavKey())
            },
            onNavigateToNavigation = {
              backStack.add(RescueNavigationNavKey())
            },
            onCreateSos = { backStack.add(SosCreateNavKey) },
            authViewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<CommunityMapNavKey> {
          VictimMainScreen(
            initialTab = VictimNavTab.MAP,
            onNavigateToLogin = {
              backStack.clear()
              backStack.add(LoginNavKey)
            },
            onNavigateToTracking = {
              backStack.add(LiveTrackingNavKey())
            },
            onNavigateToNavigation = {
              backStack.add(RescueNavigationNavKey())
            },
            onCreateSos = { backStack.add(SosCreateNavKey) },
            authViewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<LiveTrackingNavKey> { key ->
          IncidentScreen(initialId = key.incidentId, onBack = { backStack.removeLastOrNull() })
        }

        entry<RescueNavigationNavKey> { key ->
          IncidentScreen(initialId = key.incidentId, onBack = { backStack.removeLastOrNull() })
        }

        entry<AiCrisisAssistantNavKey> {
          VictimMainScreen(
            initialTab = VictimNavTab.CHAT,
            onNavigateToLogin = {
              backStack.clear()
              backStack.add(LoginNavKey)
            },
            onNavigateToTracking = {
              backStack.add(LiveTrackingNavKey())
            },
            onNavigateToNavigation = {
              backStack.add(RescueNavigationNavKey())
            },
            onCreateSos = { backStack.add(SosCreateNavKey) },
            authViewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}

