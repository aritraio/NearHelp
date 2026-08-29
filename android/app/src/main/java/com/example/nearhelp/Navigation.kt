package com.example.nearhelp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.screens.LoginScreen
import com.example.nearhelp.ui.auth.screens.PhoneOtpScreen
import com.example.nearhelp.ui.auth.screens.SignUpScreen
import com.example.nearhelp.ui.auth.screens.SplashScreen
import com.example.nearhelp.ui.home.HomeScreen

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

        entry<HomeNavKey> {
          HomeScreen(
            onNavigateToLogin = {
              backStack.clear()
              backStack.add(LoginNavKey)
            },
            viewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}
