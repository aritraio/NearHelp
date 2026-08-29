package com.example.nearhelp.data.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
  @SerializedName("id") val id: String? = null,
  @SerializedName("name") val name: String,
  @SerializedName("phone") val phone: String,
  @SerializedName("relationship") val relationship: String,
  @SerializedName("is_primary") val isPrimary: Boolean = false,
)

@Serializable
data class EmergencyContactCreateRequest(
  @SerializedName("name") val name: String,
  @SerializedName("phone") val phone: String,
  @SerializedName("relationship") val relationship: String,
  @SerializedName("is_primary") val isPrimary: Boolean = false,
)

@Serializable
data class EmergencyContactUpdateRequest(
  @SerializedName("name") val name: String? = null,
  @SerializedName("phone") val phone: String? = null,
  @SerializedName("relationship") val relationship: String? = null,
  @SerializedName("is_primary") val isPrimary: Boolean? = null,
)

@Serializable
data class MedicalIdResponse(
  @SerializedName("blood_group") val bloodGroup: String? = null,
  @SerializedName("medical_conditions") val medicalConditions: List<String> = emptyList(),
  @SerializedName("known_allergies") val knownAllergies: List<String> = emptyList(),
  @SerializedName("has_pacemaker") val hasPacemaker: Boolean = false,
  @SerializedName("is_organ_donor") val isOrganDonor: Boolean = false,
  @SerializedName("medical_notes") val medicalNotes: String? = null,
  @SerializedName("emergency_contacts") val emergencyContacts: List<EmergencyContact> = emptyList(),
  @SerializedName("is_encrypted_at_rest") val isEncryptedAtRest: Boolean = true,
)

@Serializable
data class MedicalIdUpdateRequest(
  @SerializedName("blood_group") val bloodGroup: String? = null,
  @SerializedName("medical_conditions") val medicalConditions: List<String>? = null,
  @SerializedName("known_allergies") val knownAllergies: List<String>? = null,
  @SerializedName("has_pacemaker") val hasPacemaker: Boolean? = null,
  @SerializedName("is_organ_donor") val isOrganDonor: Boolean? = null,
  @SerializedName("medical_notes") val medicalNotes: String? = null,
)

@Serializable
data class UserProfileUpdateRequest(
  @SerializedName("name") val name: String? = null,
  @SerializedName("phone") val phone: String? = null,
  @SerializedName("photo_url") val photoUrl: String? = null,
  @SerializedName("blood_group") val bloodGroup: String? = null,
  @SerializedName("languages") val languages: List<String>? = null,
  @SerializedName("has_pacemaker") val hasPacemaker: Boolean? = null,
  @SerializedName("is_organ_donor") val isOrganDonor: Boolean? = null,
  @SerializedName("medical_notes") val medicalNotes: String? = null,
  @SerializedName("medical_conditions") val medicalConditions: List<String>? = null,
  @SerializedName("known_allergies") val knownAllergies: List<String>? = null,
)

@Serializable
data class LanguagePreferencesRequest(
  @SerializedName("languages") val languages: List<String>,
)

@Serializable
data class PhotoUploadResponse(
  @SerializedName("photo_url") val photoUrl: String,
  @SerializedName("message") val message: String = "Photo uploaded successfully",
  @SerializedName("success") val success: Boolean = true,
)

@Serializable
data class SkillClaimRequest(
  @SerializedName("skill_type") val skillType: String,
  @SerializedName("certificate_url") val certificateUrl: String? = null,
  @SerializedName("notes") val notes: String? = null,
)

@Serializable
data class SkillVerificationResponse(
  @SerializedName("id") val id: String,
  @SerializedName("user_id") val userId: String,
  @SerializedName("user_name") val userName: String? = null,
  @SerializedName("user_email") val userEmail: String? = null,
  @SerializedName("user_phone") val userPhone: String? = null,
  @SerializedName("skill_type") val skillType: String,
  @SerializedName("certificate_url") val certificateUrl: String,
  @SerializedName("status") val status: String = "PENDING",
  @SerializedName("rejection_reason") val rejectionReason: String? = null,
  @SerializedName("notes") val notes: String? = null,
  @SerializedName("reviewed_by") val reviewedBy: String? = null,
  @SerializedName("submitted_at") val submittedAt: String? = null,
  @SerializedName("reviewed_at") val reviewedAt: String? = null,
)

@Serializable
data class SkillCertificateUploadResponse(
  @SerializedName("certificate_url") val certificateUrl: String,
  @SerializedName("filename") val filename: String,
  @SerializedName("file_type") val fileType: String,
  @SerializedName("message") val message: String = "Certificate uploaded successfully.",
  @SerializedName("success") val success: Boolean = true,
)

@Serializable
data class SkillVerificationReviewRequest(
  @SerializedName("action") val action: String? = null,
  @SerializedName("status") val status: String? = null,
  @SerializedName("rejection_reason") val rejectionReason: String? = null,
  @SerializedName("notes") val notes: String? = null,
)

@Serializable
data class SkillVerificationListResponse(
  @SerializedName("total") val total: Int,
  @SerializedName("verifications") val verifications: List<SkillVerificationResponse> = emptyList(),
)

