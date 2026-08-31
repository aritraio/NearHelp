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
import com.example.nearhelp.ui.map.CommunityGeoMapScreen
import com.example.nearhelp.ui.map.CommunityGeoMapViewModel
import com.example.nearhelp.ui.profile.ProfileScreen
import com.example.nearhelp.ui.profile.ProfileViewModel

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
            onNavigateToProfile = {
              backStack.add(ProfileNavKey)
            },
            onNavigateToMap = {
              backStack.add(CommunityMapNavKey)
            },
            viewModel = authViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<ProfileNavKey> {
          val profileViewModel: ProfileViewModel = viewModel {
            ProfileViewModel(NearHelpApplication.instance.userRepository)
          }
          ProfileScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            viewModel = profileViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }

        entry<CommunityMapNavKey> {
          val mapViewModel: CommunityGeoMapViewModel = viewModel()
          CommunityGeoMapScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            viewModel = mapViewModel,
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}
