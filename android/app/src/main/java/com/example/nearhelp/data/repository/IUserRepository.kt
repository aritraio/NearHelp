package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.data.api.models.MedicalIdResponse
import com.example.nearhelp.data.api.models.UserResponse

interface IUserRepository {
  suspend fun getProfile(): Result<UserResponse>

  suspend fun updateProfile(
    name: String? = null,
    phone: String? = null,
    photoUrl: String? = null,
    bloodGroup: String? = null,
    languages: List<String>? = null,
    hasPacemaker: Boolean? = null,
    isOrganDonor: Boolean? = null,
    medicalNotes: String? = null,
    medicalConditions: List<String>? = null,
    knownAllergies: List<String>? = null,
  ): Result<UserResponse>

  suspend fun getMedicalId(): Result<MedicalIdResponse>

  suspend fun updateMedicalId(
    bloodGroup: String? = null,
    medicalConditions: List<String>? = null,
    knownAllergies: List<String>? = null,
    hasPacemaker: Boolean? = null,
    isOrganDonor: Boolean? = null,
    medicalNotes: String? = null,
  ): Result<MedicalIdResponse>

  suspend fun listEmergencyContacts(): Result<List<EmergencyContact>>

  suspend fun addEmergencyContact(
    name: String,
    phone: String,
    relationship: String,
    isPrimary: Boolean = false,
  ): Result<EmergencyContact>

  suspend fun updateEmergencyContact(
    contactId: String,
    name: String? = null,
    phone: String? = null,
    relationship: String? = null,
    isPrimary: Boolean? = null,
  ): Result<EmergencyContact>

  suspend fun deleteEmergencyContact(contactId: String): Result<Boolean>

  suspend fun updateLanguages(languages: List<String>): Result<UserResponse>

  fun getStoredBloodGroup(): String?
  fun getStoredUserName(): String?
  fun getStoredUserEmail(): String?
  fun isAnonymous(): Boolean
}
