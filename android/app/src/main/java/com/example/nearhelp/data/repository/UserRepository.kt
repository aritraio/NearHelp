package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.UserApiService
import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.data.api.models.EmergencyContactCreateRequest
import com.example.nearhelp.data.api.models.EmergencyContactUpdateRequest
import com.example.nearhelp.data.api.models.LanguagePreferencesRequest
import com.example.nearhelp.data.api.models.MedicalIdResponse
import com.example.nearhelp.data.api.models.MedicalIdUpdateRequest
import com.example.nearhelp.data.api.models.UserProfileUpdateRequest
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.local.ITokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
  private val apiService: UserApiService,
  private val tokenStorage: ITokenStorage,
) : IUserRepository {

  private fun getBearerToken(): String {
    val token = tokenStorage.getAccessToken() ?: ""
    return if (token.startsWith("Bearer ")) token else "Bearer $token"
  }

  override suspend fun getProfile(): Result<UserResponse> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getCurrentUserProfile(getBearerToken())
      if (response.isSuccessful && response.body() != null) {
        val user = response.body()!!
        Result.success(user)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to load profile (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun updateProfile(
    name: String?,
    phone: String?,
    photoUrl: String?,
    bloodGroup: String?,
    languages: List<String>?,
    hasPacemaker: Boolean?,
    isOrganDonor: Boolean?,
    medicalNotes: String?,
    medicalConditions: List<String>?,
    knownAllergies: List<String>?,
  ): Result<UserResponse> = withContext(Dispatchers.IO) {
    try {
      val req = UserProfileUpdateRequest(
        name = name,
        phone = phone,
        photoUrl = photoUrl,
        bloodGroup = bloodGroup,
        languages = languages,
        hasPacemaker = hasPacemaker,
        isOrganDonor = isOrganDonor,
        medicalNotes = medicalNotes,
        medicalConditions = medicalConditions,
        knownAllergies = knownAllergies,
      )
      val response = apiService.updateUserProfile(getBearerToken(), req)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to update profile (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun getMedicalId(): Result<MedicalIdResponse> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getMedicalId(getBearerToken())
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to load medical ID (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun updateMedicalId(
    bloodGroup: String?,
    medicalConditions: List<String>?,
    knownAllergies: List<String>?,
    hasPacemaker: Boolean?,
    isOrganDonor: Boolean?,
    medicalNotes: String?,
  ): Result<MedicalIdResponse> = withContext(Dispatchers.IO) {
    try {
      val req = MedicalIdUpdateRequest(
        bloodGroup = bloodGroup,
        medicalConditions = medicalConditions,
        knownAllergies = knownAllergies,
        hasPacemaker = hasPacemaker,
        isOrganDonor = isOrganDonor,
        medicalNotes = medicalNotes,
      )
      val response = apiService.updateMedicalId(getBearerToken(), req)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to update medical ID (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun listEmergencyContacts(): Result<List<EmergencyContact>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.listEmergencyContacts(getBearerToken())
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to list contacts (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun addEmergencyContact(
    name: String,
    phone: String,
    relationship: String,
    isPrimary: Boolean,
  ): Result<EmergencyContact> = withContext(Dispatchers.IO) {
    try {
      val req = EmergencyContactCreateRequest(
        name = name.trim(),
        phone = phone.trim(),
        relationship = relationship.trim(),
        isPrimary = isPrimary,
      )
      val response = apiService.addEmergencyContact(getBearerToken(), req)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to add emergency contact (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun updateEmergencyContact(
    contactId: String,
    name: String?,
    phone: String?,
    relationship: String?,
    isPrimary: Boolean?,
  ): Result<EmergencyContact> = withContext(Dispatchers.IO) {
    try {
      val req = EmergencyContactUpdateRequest(
        name = name?.trim(),
        phone = phone?.trim(),
        relationship = relationship?.trim(),
        isPrimary = isPrimary,
      )
      val response = apiService.updateEmergencyContact(getBearerToken(), contactId, req)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to update contact (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun deleteEmergencyContact(contactId: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.deleteEmergencyContact(getBearerToken(), contactId)
      if (response.isSuccessful) {
        Result.success(true)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to delete contact (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun updateLanguages(languages: List<String>): Result<UserResponse> = withContext(Dispatchers.IO) {
    try {
      val req = LanguagePreferencesRequest(languages = languages)
      val response = apiService.updateLanguages(getBearerToken(), req)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to update languages (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override fun getStoredBloodGroup(): String? = tokenStorage.getBloodGroup()
  override fun getStoredUserName(): String? = tokenStorage.getUserName()
  override fun getStoredUserEmail(): String? = tokenStorage.getUserEmail()
  override fun isAnonymous(): Boolean = tokenStorage.isAnonymous()
}
