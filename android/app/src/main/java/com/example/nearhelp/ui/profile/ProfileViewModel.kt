package com.example.nearhelp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.data.api.models.MedicalIdResponse
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.repository.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import android.content.Context
import android.net.Uri
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.api.SessionTokens
import com.example.nearhelp.data.api.models.SkillVerificationResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody

data class ProfileUiState(
  val isLoading: Boolean = false,
  val isSaving: Boolean = false,
  val user: UserResponse? = null,
  val medicalId: MedicalIdResponse? = null,
  val emergencyContacts: List<EmergencyContact> = emptyList(),
  val error: String? = null,
  val successMessage: String? = null,
  val showEditProfileDialog: Boolean = false,
  val showEditMedicalIdDialog: Boolean = false,
  val showAddContactDialog: Boolean = false,
  val editingContact: EmergencyContact? = null,
  val showQualificationsDialog: Boolean = false,
  val showEmergencyHistoryDialog: Boolean = false,
  val skillClaims: List<SkillVerificationResponse> = emptyList(),
)

class ProfileViewModel(
  private val userRepository: IUserRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUiState())
  val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

  init {
    val cachedUser = userRepository.getCachedProfile()
    if (cachedUser != null) {
      _uiState.value = ProfileUiState(
        isLoading = false,
        user = cachedUser,
        emergencyContacts = cachedUser.emergencyContacts,
        medicalId = MedicalIdResponse(
          bloodGroup = cachedUser.bloodGroup,
          medicalConditions = cachedUser.medicalConditions,
          knownAllergies = cachedUser.knownAllergies,
          hasPacemaker = cachedUser.hasPacemaker,
          isOrganDonor = cachedUser.isOrganDonor,
          medicalNotes = cachedUser.medicalNotes,
          emergencyContacts = cachedUser.emergencyContacts,
          isEncryptedAtRest = true,
        ),
      )
    }
    loadProfile()
  }

  fun loadProfile() {
    viewModelScope.launch {
      if (_uiState.value.user == null) {
        _uiState.update { it.copy(isLoading = true, error = null) }
      }
      val result = userRepository.getProfile()
      result.fold(
        onSuccess = { user ->
          _uiState.update {
            it.copy(
              isLoading = false,
              user = user,
              emergencyContacts = user.emergencyContacts,
              medicalId = MedicalIdResponse(
                bloodGroup = user.bloodGroup,
                medicalConditions = user.medicalConditions,
                knownAllergies = user.knownAllergies,
                hasPacemaker = user.hasPacemaker,
                isOrganDonor = user.isOrganDonor,
                medicalNotes = user.medicalNotes,
                emergencyContacts = user.emergencyContacts,
                isEncryptedAtRest = true,
              ),
              error = null,
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isLoading = false,
              error = if (it.user == null) (err.message ?: "Failed to load profile") else null,
            )
          }
        },
      )
    }
  }

  fun updateProfile(
    name: String?,
    phone: String?,
    bloodGroup: String?,
    languages: List<String>?,
    hasPacemaker: Boolean?,
    isOrganDonor: Boolean?,
    medicalNotes: String?,
    medicalConditions: List<String>?,
    knownAllergies: List<String>?,
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      val result = userRepository.updateProfile(
        name = name,
        phone = phone,
        bloodGroup = bloodGroup,
        languages = languages,
        hasPacemaker = hasPacemaker,
        isOrganDonor = isOrganDonor,
        medicalNotes = medicalNotes,
        medicalConditions = medicalConditions,
        knownAllergies = knownAllergies,
      )
      result.fold(
        onSuccess = { updatedUser ->
          _uiState.update {
            it.copy(
              isSaving = false,
              user = updatedUser,
              emergencyContacts = updatedUser.emergencyContacts,
              showEditProfileDialog = false,
              successMessage = "Profile updated successfully.",
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isSaving = false,
              error = err.message ?: "Failed to update profile",
            )
          }
        },
      )
    }
  }

  fun updateMedicalId(
    bloodGroup: String?,
    conditions: List<String>?,
    allergies: List<String>?,
    hasPacemaker: Boolean?,
    isOrganDonor: Boolean?,
    notes: String?,
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      val result = userRepository.updateMedicalId(
        bloodGroup = bloodGroup,
        medicalConditions = conditions,
        knownAllergies = allergies,
        hasPacemaker = hasPacemaker,
        isOrganDonor = isOrganDonor,
        medicalNotes = notes,
      )
      result.fold(
        onSuccess = { updatedMedId ->
          _uiState.update { state ->
            val updatedUser = state.user?.copy(
              bloodGroup = updatedMedId.bloodGroup,
              medicalConditions = updatedMedId.medicalConditions,
              knownAllergies = updatedMedId.knownAllergies,
              hasPacemaker = updatedMedId.hasPacemaker,
              isOrganDonor = updatedMedId.isOrganDonor,
              medicalNotes = updatedMedId.medicalNotes,
            )
            state.copy(
              isSaving = false,
              medicalId = updatedMedId,
              user = updatedUser,
              showEditMedicalIdDialog = false,
              successMessage = "Encrypted Medical ID updated & secured with AES-256.",
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isSaving = false,
              error = err.message ?: "Failed to update Medical ID",
            )
          }
        },
      )
    }
  }

  fun addEmergencyContact(
    name: String,
    phone: String,
    relationship: String,
    isPrimary: Boolean = false,
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      val result = userRepository.addEmergencyContact(name, phone, relationship, isPrimary)
      result.fold(
        onSuccess = { newContact ->
          _uiState.update { state ->
            val updatedList = state.emergencyContacts.toMutableList()
            if (isPrimary) {
              for (i in updatedList.indices) {
                updatedList[i] = updatedList[i].copy(isPrimary = false)
              }
            }
            updatedList.add(newContact)
            state.copy(
              isSaving = false,
              emergencyContacts = updatedList,
              showAddContactDialog = false,
              successMessage = "Emergency contact added successfully.",
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isSaving = false,
              error = err.message ?: "Failed to add emergency contact",
            )
          }
        },
      )
    }
  }

  fun updateEmergencyContact(
    contactId: String,
    name: String?,
    phone: String?,
    relationship: String?,
    isPrimary: Boolean?,
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      val result = userRepository.updateEmergencyContact(contactId, name, phone, relationship, isPrimary)
      result.fold(
        onSuccess = { updated ->
          _uiState.update { state ->
            val updatedList = state.emergencyContacts.map { c ->
              if (c.id == contactId) {
                updated
              } else if (isPrimary == true) {
                c.copy(isPrimary = false)
              } else {
                c
              }
            }
            state.copy(
              isSaving = false,
              emergencyContacts = updatedList,
              editingContact = null,
              successMessage = "Emergency contact updated.",
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isSaving = false,
              error = err.message ?: "Failed to update emergency contact",
            )
          }
        },
      )
    }
  }

  fun deleteEmergencyContact(contactId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      val result = userRepository.deleteEmergencyContact(contactId)
      result.fold(
        onSuccess = {
          _uiState.update { state ->
            val updatedList = state.emergencyContacts.filter { it.id != contactId }
            state.copy(
              isSaving = false,
              emergencyContacts = updatedList,
              successMessage = "Emergency contact removed.",
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isSaving = false,
              error = err.message ?: "Failed to delete emergency contact",
            )
          }
        },
      )
    }
  }

  fun updateLanguages(languages: List<String>) {
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      val result = userRepository.updateLanguages(languages)
      result.fold(
        onSuccess = { updatedUser ->
          _uiState.update {
            it.copy(
              isSaving = false,
              user = updatedUser,
              successMessage = "Language preferences updated.",
            )
          }
        },
        onFailure = { err ->
          _uiState.update {
            it.copy(
              isSaving = false,
              error = err.message ?: "Failed to update languages",
            )
          }
        },
      )
    }
  }

  fun openEditProfileDialog() = _uiState.update { it.copy(showEditProfileDialog = true) }
  fun closeEditProfileDialog() = _uiState.update { it.copy(showEditProfileDialog = false) }

  fun openEditMedicalIdDialog() = _uiState.update { it.copy(showEditMedicalIdDialog = true) }
  fun closeEditMedicalIdDialog() = _uiState.update { it.copy(showEditMedicalIdDialog = false) }

  fun openAddContactDialog() = _uiState.update { it.copy(showAddContactDialog = true) }
  fun closeAddContactDialog() = _uiState.update { it.copy(showAddContactDialog = false) }

  fun openEditContactDialog(contact: EmergencyContact) = _uiState.update { it.copy(editingContact = contact) }
  fun closeEditContactDialog() = _uiState.update { it.copy(editingContact = null) }

  fun openQualificationsDialog() = _uiState.update { it.copy(showQualificationsDialog = true) }
  fun closeQualificationsDialog() = _uiState.update { it.copy(showQualificationsDialog = false) }

  fun openEmergencyHistoryDialog() = _uiState.update { it.copy(showEmergencyHistoryDialog = true) }
  fun closeEmergencyHistoryDialog() = _uiState.update { it.copy(showEmergencyHistoryDialog = false) }

  fun saveQualification(title: String, organization: String, idNumber: String) {
    _uiState.update { it.copy(error = "Attach your certificate before submitting a qualification.") }
  }

  fun loadSkillClaims() {
    viewModelScope.launch {
      try {
        val result = RetrofitClient.getUserApiService().listMySkills(SessionTokens.bearer())
        if (result.isSuccessful) _uiState.update { it.copy(skillClaims = result.body().orEmpty()) }
      } catch (e: CancellationException) { throw e }
      catch (_: Exception) { /* Profile data remains available while offline. */ }
    }
  }

  fun submitQualification(title: String, organization: String, idNumber: String, uri: Uri?, context: Context) {
    if (_uiState.value.isSaving) return
    if (uri == null) { _uiState.update { it.copy(error = "Attach your certificate before submitting.") }; return }
    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      try {
        val result = withContext(Dispatchers.IO) {
          val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
          val rawExtension = mapOf("application/pdf" to "pdf", "image/jpeg" to "jpg", "image/png" to "png", "image/webp" to "webp")[mime]
            ?: error("Choose a PDF, PNG, JPG or WebP certificate.")

          val (uploadBytes, uploadMime, uploadExtension) = if (mime.startsWith("image/") || rawExtension in listOf("jpg", "jpeg", "png", "webp")) {
            val compressed = runCatching {
              val boundsOptions = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
              context.contentResolver.openInputStream(uri)?.use { input ->
                android.graphics.BitmapFactory.decodeStream(input, null, boundsOptions)
              }
              if (boundsOptions.outWidth > 0 && boundsOptions.outHeight > 0) {
                var inSampleSize = 1
                val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
                while (maxDim / inSampleSize > 1600) { inSampleSize *= 2 }
                val decodeOptions = android.graphics.BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
                val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                  android.graphics.BitmapFactory.decodeStream(input, null, decodeOptions)
                }
                if (bitmap != null) {
                  val bos = java.io.ByteArrayOutputStream()
                  bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 82, bos)
                  bitmap.recycle()
                  Triple(bos.toByteArray(), "image/jpeg", "jpg")
                } else null
              } else null
            }.getOrNull()

            compressed ?: run {
              val output = java.io.ByteArrayOutputStream()
              context.contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                  val count = input.read(buffer); if (count < 0) break
                  check(output.size() + count <= 10 * 1024 * 1024) { "Certificate must be smaller than 10 MB." }
                  output.write(buffer, 0, count)
                }
              } ?: error("Could not open the certificate.")
              Triple(output.toByteArray(), mime, rawExtension)
            }
          } else {
            val output = java.io.ByteArrayOutputStream()
            context.contentResolver.openInputStream(uri)?.use { input ->
              val buffer = ByteArray(8192)
              while (true) {
                val count = input.read(buffer); if (count < 0) break
                check(output.size() + count <= 10 * 1024 * 1024) { "Certificate must be smaller than 10 MB." }
                output.write(buffer, 0, count)
              }
            } ?: error("Could not open the certificate.")
            Triple(output.toByteArray(), mime, rawExtension)
          }

          val code = when (title) { "CPR / AED certified" -> "CPR_CERTIFIED"; "First-aid provider" -> "FIRST_AID"; "EMT / Paramedic" -> "EMT"; "Doctor / Physician" -> "DOCTOR"; "Registered nurse" -> "NURSE"; else -> "COMMUNITY_FIRST_RESPONDER" }
          RetrofitClient.getUserApiService().claimSkillWithCertificate(SessionTokens.bearer(), code.toRequestBody("text/plain".toMediaType()),
            MultipartBody.Part.createFormData("file", "certificate.$uploadExtension", uploadBytes.toRequestBody(uploadMime.toMediaType())),
            "Issuing organization: $organization\nCertificate ID: $idNumber".toRequestBody("text/plain".toMediaType()))
        }
        check(result.isSuccessful) { "The certificate could not be submitted (${result.code()}). Please try again." }
        val claim = result.body() ?: error("The server returned an empty response.")
        _uiState.update { it.copy(isSaving = false, skillClaims = it.skillClaims + claim, successMessage = "Qualification submitted for verification.") }
      } catch (e: CancellationException) { throw e }
      catch (e: Exception) { _uiState.update { it.copy(isSaving = false, error = e.message ?: "Could not submit certificate.") } }
    }
  }

  fun clearError() = _uiState.update { it.copy(error = null) }
  fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }

  fun isAnonymous(): Boolean = userRepository.isAnonymous()
  fun getStoredUserName(): String? = userRepository.getStoredUserName()
  fun getStoredUserEmail(): String? = userRepository.getStoredUserEmail()
}
