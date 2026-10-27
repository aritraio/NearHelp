package com.example.nearhelp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.nearhelp.ui.incident.IncidentScreen
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

private const val NAV_TRANSITION_DURATION = 280

private fun isBottomNavKey(key: Any): Boolean =
  key is HomeNavKey || key is ProfileNavKey || key is CommunityMapNavKey || key is AiCrisisAssistantNavKey

private fun getBottomNavIndex(key: Any): Int = when (key) {
  is HomeNavKey -> 0
  is AiCrisisAssistantNavKey -> 1
  is CommunityMapNavKey -> 2
  is ProfileNavKey -> 3
  else -> 0
}

private fun <T : Any> appTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
  if (initialState.key is SplashNavKey || targetState.key is SplashNavKey) {
    (fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing)) +
     scaleIn(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing), initialScale = 0.96f)) togetherWith
    (fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing)) +
     scaleOut(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing), targetScale = 1.04f))
  } else if (isBottomNavKey(initialState.key) && isBottomNavKey(targetState.key)) {
    val isForward = getBottomNavIndex(targetState.key) >= getBottomNavIndex(initialState.key)
    if (isForward) {
      (slideInHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialOffsetX = { it / 5 }
      ) + scaleIn(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialScale = 0.95f
      ) + fadeIn(
        animationSpec = tween(NAV_TRANSITION_DURATION - 40, easing = FastOutSlowInEasing)
      )) togetherWith
      (slideOutHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetOffsetX = { -it / 6 }
      ) + scaleOut(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetScale = 0.96f
      ) + fadeOut(
        animationSpec = tween(NAV_TRANSITION_DURATION - 60, easing = FastOutSlowInEasing)
      ))
    } else {
      (slideInHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialOffsetX = { -it / 5 }
      ) + scaleIn(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialScale = 0.95f
      ) + fadeIn(
        animationSpec = tween(NAV_TRANSITION_DURATION - 40, easing = FastOutSlowInEasing)
      )) togetherWith
      (slideOutHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetOffsetX = { it / 6 }
      ) + scaleOut(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetScale = 0.96f
      ) + fadeOut(
        animationSpec = tween(NAV_TRANSITION_DURATION - 60, easing = FastOutSlowInEasing)
      ))
    }
  } else {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> fullWidth }
    ) + scaleIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialScale = 0.95f
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.6f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> -fullWidth / 4 }
    ) + scaleOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetScale = 0.92f
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.7f
    ))
  }
}

private fun <T : Any> appPopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
  if (initialState.key is SplashNavKey || targetState.key is SplashNavKey) {
    (fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing)) +
     scaleIn(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing), initialScale = 0.96f)) togetherWith
    (fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing)) +
     scaleOut(animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing), targetScale = 1.04f))
  } else if (isBottomNavKey(initialState.key) && isBottomNavKey(targetState.key)) {
    val isForward = getBottomNavIndex(targetState.key) >= getBottomNavIndex(initialState.key)
    if (isForward) {
      (slideInHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialOffsetX = { it / 5 }
      ) + scaleIn(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialScale = 0.95f
      ) + fadeIn(
        animationSpec = tween(NAV_TRANSITION_DURATION - 40, easing = FastOutSlowInEasing)
      )) togetherWith
      (slideOutHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetOffsetX = { -it / 6 }
      ) + scaleOut(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetScale = 0.96f
      ) + fadeOut(
        animationSpec = tween(NAV_TRANSITION_DURATION - 60, easing = FastOutSlowInEasing)
      ))
    } else {
      (slideInHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialOffsetX = { -it / 5 }
      ) + scaleIn(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        initialScale = 0.95f
      ) + fadeIn(
        animationSpec = tween(NAV_TRANSITION_DURATION - 40, easing = FastOutSlowInEasing)
      )) togetherWith
      (slideOutHorizontally(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetOffsetX = { it / 6 }
      ) + scaleOut(
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
        targetScale = 0.96f
      ) + fadeOut(
        animationSpec = tween(NAV_TRANSITION_DURATION - 60, easing = FastOutSlowInEasing)
      ))
    }
  } else {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> -fullWidth / 4 }
    ) + scaleIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialScale = 0.92f
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.7f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> fullWidth }
    ) + scaleOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetScale = 0.96f
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.6f
    ))
  }
}

private fun <T : Any> appPredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = { swipeEdge ->
  if (swipeEdge == NavigationEvent.EDGE_RIGHT) {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> fullWidth / 4 }
    ) + scaleIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialScale = 0.93f
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.7f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> -fullWidth }
    ) + scaleOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetScale = 0.92f
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.6f
    ))
  } else {
    (slideInHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialOffsetX = { fullWidth -> -fullWidth / 4 }
    ) + scaleIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialScale = 0.93f
    ) + fadeIn(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      initialAlpha = 0.7f
    )) togetherWith
    (slideOutHorizontally(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetOffsetX = { fullWidth -> fullWidth }
    ) + scaleOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetScale = 0.92f
    ) + fadeOut(
      animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
      targetAlpha = 0.6f
    ))
  }
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

