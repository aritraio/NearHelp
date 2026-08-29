package com.example.nearhelp.data.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class SkillItem(
  @SerializedName("skill_type") val skillType: String,
  @SerializedName("verified") val verified: Boolean = false,
  @SerializedName("certificate_url") val certificateUrl: String? = null,
  @SerializedName("verified_at") val verifiedAt: String? = null,
)

@Serializable
data class UserResponse(
  @SerializedName("id") val id: String,
  @SerializedName("email") val email: String? = null,
  @SerializedName("name") val name: String? = null,
  @SerializedName("photo_url") val photoUrl: String? = null,
  @SerializedName("phone") val phone: String? = null,
  @SerializedName("blood_group") val bloodGroup: String? = null,
  @SerializedName("languages") val languages: List<String> = listOf("en"),
  @SerializedName("phone_verified") val phoneVerified: Boolean = false,
  @SerializedName("auth_provider") val authProvider: String = "email",
  @SerializedName("is_anonymous") val isAnonymous: Boolean = false,
  @SerializedName("is_active") val isActive: Boolean = true,
  @SerializedName("has_pacemaker") val hasPacemaker: Boolean = false,
  @SerializedName("is_organ_donor") val isOrganDonor: Boolean = false,
  @SerializedName("medical_notes") val medicalNotes: String? = null,
  @SerializedName("medical_conditions") val medicalConditions: List<String> = emptyList(),
  @SerializedName("known_allergies") val knownAllergies: List<String> = emptyList(),
  @SerializedName("trust_score") val trustScore: Double = 50.0,
  @SerializedName("badges") val badges: List<String> = emptyList(),
  @SerializedName("emergency_contacts") val emergencyContacts: List<EmergencyContact> = emptyList(),
  @SerializedName("skills") val skills: List<SkillItem> = emptyList(),
  @SerializedName("fcm_token") val fcmToken: String? = null,
  @SerializedName("created_at") val createdAt: String? = null,
  @SerializedName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class RegisterRequest(
  @SerializedName("email") val email: String,
  @SerializedName("password") val password: String,
  @SerializedName("name") val name: String,
  @SerializedName("phone") val phone: String? = null,
  @SerializedName("blood_group") val bloodGroup: String? = null,
)

@Serializable
data class LoginRequest(
  @SerializedName("email") val email: String,
  @SerializedName("password") val password: String,
)

@Serializable
data class GoogleAuthRequest(
  @SerializedName("id_token") val idToken: String,
)

@Serializable
data class PhoneSendOtpRequest(
  @SerializedName("phone_number") val phoneNumber: String,
)

@Serializable
data class PhoneVerifyRequest(
  @SerializedName("phone_number") val phoneNumber: String,
  @SerializedName("otp_code") val otpCode: String? = null,
  @SerializedName("id_token") val idToken: String? = null,
  @SerializedName("name") val name: String? = null,
)

@Serializable
data class AnonymousAuthRequest(
  @SerializedName("device_id") val deviceId: String? = null,
  @SerializedName("temp_name") val tempName: String? = "Anonymous Victim",
)

@Serializable
data class TokenRefreshRequest(
  @SerializedName("refresh_token") val refreshToken: String,
)

@Serializable
data class TokenResponse(
  @SerializedName("access_token") val accessToken: String,
  @SerializedName("refresh_token") val refreshToken: String,
  @SerializedName("token_type") val tokenType: String = "bearer",
  @SerializedName("expires_in") val expiresIn: Int = 900,
  @SerializedName("user") val user: UserResponse,
)

@Serializable
data class MessageResponse(
  @SerializedName("message") val message: String,
  @SerializedName("success") val success: Boolean = true,
)

@Serializable
data class DeviceRegisterRequest(
  @SerializedName("fcm_token") val fcmToken: String,
  @SerializedName("platform") val platform: String = "android",
  @SerializedName("device_info") val deviceInfo: Map<String, String>? = null,
)
