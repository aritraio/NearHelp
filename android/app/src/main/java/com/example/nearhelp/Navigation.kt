package com.example.nearhelp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nearhelp.ui.assistant.AiCrisisAssistantScreen
import com.example.nearhelp.ui.assistant.AiCrisisAssistantViewModel
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.screens.LoginScreen
import com.example.nearhelp.ui.auth.screens.PhoneOtpScreen
import com.example.nearhelp.ui.auth.screens.SignUpScreen
import com.example.nearhelp.ui.auth.screens.SplashScreen
import com.example.nearhelp.ui.home.HomeScreen
import com.example.nearhelp.ui.home.VictimMainScreen
import com.example.nearhelp.ui.map.CommunityGeoMapScreen
import com.example.nearhelp.ui.map.CommunityGeoMapViewModel
import com.example.nearhelp.ui.navigation.RescueNavigationScreen
import com.example.nearhelp.ui.navigation.RescueNavigationViewModel
import com.example.nearhelp.ui.profile.ProfileScreen
import com.example.nearhelp.ui.profile.ProfileViewModel
import com.example.nearhelp.ui.tracking.LiveTrackingScreen
import com.example.nearhelp.ui.tracking.LiveTrackingViewModel
import com.example.nearhelp.ui.victim.VictimNavTab

private const val NAV_TRANSITION_DURATION = 140

private fun isBottomNavKey(key: Any): Boolean =
  key is HomeNavKey || key is ProfileNavKey || key is CommunityMapNavKey || key is AiCrisisAssistantNavKey

private fun <T : Any> appTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
  if (initialState.key is SplashNavKey || targetState.key is SplashNavKey) {
    fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION)) togetherWith
      fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION))
  } else if (isBottomNavKey(initialState.key) && isBottomNavKey(targetState.key)) {
    fadeIn(animationSpec = tween(40, easing = androidx.compose.animation.core.LinearEasing)) togetherWith
      fadeOut(animationSpec = tween(40, easing = androidx.compose.animation.core.LinearEasing))
  } else {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.8f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> -fullWidth / 4 }
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.7f
    ))
  }
}

private fun <T : Any> appPopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
  if (initialState.key is SplashNavKey || targetState.key is SplashNavKey) {
    fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION)) togetherWith
      fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION))
  } else if (isBottomNavKey(initialState.key) && isBottomNavKey(targetState.key)) {
    fadeIn(animationSpec = tween(40, easing = androidx.compose.animation.core.LinearEasing)) togetherWith
      fadeOut(animationSpec = tween(40, easing = androidx.compose.animation.core.LinearEasing))
  } else {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> -fullWidth / 4 }
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.8f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> fullWidth }
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.7f
    ))
  }
}

private fun <T : Any> appPredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = { swipeEdge ->
  if (swipeEdge == NavigationEvent.EDGE_RIGHT) {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> fullWidth / 4 }
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.8f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> -fullWidth }
    ) + scaleOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetScale = 0.94f
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.7f
    ))
  } else {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> -fullWidth / 4 }
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.8f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> fullWidth }
    ) + scaleOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetScale = 0.94f
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.7f
    ))
  }
}

@Composable
fun MainNavigation(
  authViewModel: AuthViewModel = viewModel {
    AuthViewModel(NearHelpApplication.instance.authRepository)
  }
) {
  val backStack = rememberNavBackStack(SplashNavKey)

  NavDisplay(
    backStack = backStack,
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
          com.example.nearhelp.ui.screens.CrisisDispatchScreen(
            onCancel = { backStack.removeLastOrNull() },
            onDispatch = { _, _ ->
              backStack.add(LiveTrackingNavKey())
            },
            onNavigateToAssistant = {
              backStack.add(AiCrisisAssistantNavKey())
            },
            onNavigateToMap = {
              backStack.add(CommunityMapNavKey)
            },
            modifier = Modifier.fillMaxSize(),
          )
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
            authViewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<LiveTrackingNavKey> {
          val trackingViewModel: LiveTrackingViewModel = viewModel()
          val storedToken = NearHelpApplication.instance.authRepository.getStoredAccessToken()
          LiveTrackingScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateToNavigation = {
              backStack.add(RescueNavigationNavKey())
            },
            viewModel = trackingViewModel,
            token = storedToken,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<RescueNavigationNavKey> {
          val navViewModel: RescueNavigationViewModel = viewModel {
            RescueNavigationViewModel(NearHelpApplication.instance.routingRepository)
          }
          val storedToken = NearHelpApplication.instance.authRepository.getStoredAccessToken()
          RescueNavigationScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            viewModel = navViewModel,
            token = storedToken,
            modifier = Modifier.fillMaxSize(),
          )
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
            authViewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}

