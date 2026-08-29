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
)

class ProfileViewModel(
  private val userRepository: IUserRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUiState())
  val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

  init {
    loadProfile()
  }

  fun loadProfile() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
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
              error = err.message ?: "Failed to load profile",
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

  fun clearError() = _uiState.update { it.copy(error = null) }
  fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }

  fun isAnonymous(): Boolean = userRepository.isAnonymous()
  fun getStoredUserName(): String? = userRepository.getStoredUserName()
  fun getStoredUserEmail(): String? = userRepository.getStoredUserEmail()
}
