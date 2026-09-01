package com.example.nearhelp

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SplashNavKey : NavKey
@Serializable data object LoginNavKey : NavKey
@Serializable data object SignUpNavKey : NavKey
@Serializable data object PhoneOtpNavKey : NavKey
@Serializable data object HomeNavKey : NavKey
@Serializable data object GuardianRadarNavKey : NavKey
@Serializable data object CrisisDispatchNavKey : NavKey
@Serializable data object ProfileNavKey : NavKey
@Serializable data object CommunityMapNavKey : NavKey
@Serializable data class LiveTrackingNavKey(val incidentId: String = "KOL-SOS-8821") : NavKey
@Serializable data class RescueNavigationNavKey(val incidentId: String = "KOL-SOS-8821") : NavKey
@Serializable data class AiCrisisAssistantNavKey(
  val conditionId: String = "cardiac_arrest",
  val incidentId: String = "KOL-SOS-8821"
) : NavKey


