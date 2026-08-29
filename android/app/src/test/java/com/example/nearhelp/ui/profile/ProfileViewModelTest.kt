package com.example.nearhelp.ui.profile

import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.data.api.models.MedicalIdResponse
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.repository.IUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeUserRepository
  private lateinit var viewModel: ProfileViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeUserRepository()
    viewModel = ProfileViewModel(fakeRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `loadProfile loads user and encrypted medical id successfully`() = runTest {
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNotNull(state.user)
    assertEquals("Alex Mercer", state.user?.name)
    assertEquals("O+", state.user?.bloodGroup)
    assertEquals(1, state.emergencyContacts.size)
    assertEquals("Maria Mercer", state.emergencyContacts.first().name)
    assertTrue(state.medicalId?.isEncryptedAtRest == true)
  }

  @Test
  fun `updateMedicalId updates medical id and user state`() = runTest {
    advanceUntilIdle()

    viewModel.updateMedicalId(
      bloodGroup = "AB-",
      conditions = listOf("Asthma", "Diabetes"),
      allergies = listOf("Penicillin"),
      hasPacemaker = true,
      isOrganDonor = true,
      notes = "Carries EpiPen",
    )

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isSaving)
    assertEquals("AB-", state.medicalId?.bloodGroup)
    assertEquals(listOf("Asthma", "Diabetes"), state.medicalId?.medicalConditions)
    assertEquals(listOf("Penicillin"), state.medicalId?.knownAllergies)
    assertTrue(state.medicalId?.hasPacemaker == true)
    assertTrue(state.medicalId?.isOrganDonor == true)
    assertEquals("Carries EpiPen", state.medicalId?.medicalNotes)
    assertNotNull(state.successMessage)
  }

  @Test
  fun `addEmergencyContact adds contact to state`() = runTest {
    advanceUntilIdle()

    viewModel.addEmergencyContact(
      name = "Elena Mercer",
      phone = "+919830099887",
      relationship = "Spouse",
      isPrimary = true,
    )

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isSaving)
    assertEquals(2, state.emergencyContacts.size)
    val added = state.emergencyContacts.find { it.name == "Elena Mercer" }
    assertNotNull(added)
    assertTrue(added?.isPrimary == true)
  }

  @Test
  fun `deleteEmergencyContact removes contact from state`() = runTest {
    advanceUntilIdle()

    val contactId = stateContactId()
    viewModel.deleteEmergencyContact(contactId)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.emergencyContacts.isEmpty())
  }

  @Test
  fun `updateLanguages updates language preferences`() = runTest {
    advanceUntilIdle()

    viewModel.updateLanguages(listOf("en", "bn", "hi"))

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(listOf("en", "bn", "hi"), state.user?.languages)
  }

  @Test
  fun `dialog visibility helpers toggle state properly`() {
    viewModel.openEditProfileDialog()
    assertTrue(viewModel.uiState.value.showEditProfileDialog)
    viewModel.closeEditProfileDialog()
    assertFalse(viewModel.uiState.value.showEditProfileDialog)

    viewModel.openEditMedicalIdDialog()
    assertTrue(viewModel.uiState.value.showEditMedicalIdDialog)
    viewModel.closeEditMedicalIdDialog()
    assertFalse(viewModel.uiState.value.showEditMedicalIdDialog)

    viewModel.openAddContactDialog()
    assertTrue(viewModel.uiState.value.showAddContactDialog)
    viewModel.closeAddContactDialog()
    assertFalse(viewModel.uiState.value.showAddContactDialog)
  }

  private fun stateContactId(): String {
    return viewModel.uiState.value.emergencyContacts.first().id ?: "c1"
  }
}

class FakeUserRepository : IUserRepository {

  var shouldFail: Boolean = false

  var mockUser = UserResponse(
    id = "user-uuid-1",
    email = "alex@nearhelp.ai",
    name = "Alex Mercer",
    phone = "+919830011223",
    bloodGroup = "O+",
    languages = listOf("en"),
    emergencyContacts = listOf(
      EmergencyContact(
        id = "c1",
        name = "Maria Mercer",
        phone = "+919830011223",
        relationship = "Mother",
        isPrimary = true,
      )
    ),
    medicalConditions = listOf("Asthma"),
    knownAllergies = listOf("Penicillin"),
    hasPacemaker = false,
    isOrganDonor = true,
    medicalNotes = "Standard medical record",
  )

  override suspend fun getProfile(): Result<UserResponse> {
    return if (shouldFail) Result.failure(Exception("Network error")) else Result.success(mockUser)
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
  ): Result<UserResponse> {
    if (shouldFail) return Result.failure(Exception("Update failed"))
    mockUser = mockUser.copy(
      name = name ?: mockUser.name,
      phone = phone ?: mockUser.phone,
      photoUrl = photoUrl ?: mockUser.photoUrl,
      bloodGroup = bloodGroup ?: mockUser.bloodGroup,
      languages = languages ?: mockUser.languages,
      hasPacemaker = hasPacemaker ?: mockUser.hasPacemaker,
      isOrganDonor = isOrganDonor ?: mockUser.isOrganDonor,
      medicalNotes = medicalNotes ?: mockUser.medicalNotes,
      medicalConditions = medicalConditions ?: mockUser.medicalConditions,
      knownAllergies = knownAllergies ?: mockUser.knownAllergies,
    )
    return Result.success(mockUser)
  }

  override suspend fun getMedicalId(): Result<MedicalIdResponse> {
    if (shouldFail) return Result.failure(Exception("Get medical ID failed"))
    return Result.success(
      MedicalIdResponse(
        bloodGroup = mockUser.bloodGroup,
        medicalConditions = mockUser.medicalConditions,
        knownAllergies = mockUser.knownAllergies,
        hasPacemaker = mockUser.hasPacemaker,
        isOrganDonor = mockUser.isOrganDonor,
        medicalNotes = mockUser.medicalNotes,
        emergencyContacts = mockUser.emergencyContacts,
        isEncryptedAtRest = true,
      )
    )
  }

  override suspend fun updateMedicalId(
    bloodGroup: String?,
    medicalConditions: List<String>?,
    knownAllergies: List<String>?,
    hasPacemaker: Boolean?,
    isOrganDonor: Boolean?,
    medicalNotes: String?,
  ): Result<MedicalIdResponse> {
    if (shouldFail) return Result.failure(Exception("Update medical ID failed"))
    mockUser = mockUser.copy(
      bloodGroup = bloodGroup ?: mockUser.bloodGroup,
      medicalConditions = medicalConditions ?: mockUser.medicalConditions,
      knownAllergies = knownAllergies ?: mockUser.knownAllergies,
      hasPacemaker = hasPacemaker ?: mockUser.hasPacemaker,
      isOrganDonor = isOrganDonor ?: mockUser.isOrganDonor,
      medicalNotes = medicalNotes ?: mockUser.medicalNotes,
    )
    return getMedicalId()
  }

  override suspend fun listEmergencyContacts(): Result<List<EmergencyContact>> {
    if (shouldFail) return Result.failure(Exception("List contacts failed"))
    return Result.success(mockUser.emergencyContacts)
  }

  override suspend fun addEmergencyContact(
    name: String,
    phone: String,
    relationship: String,
    isPrimary: Boolean,
  ): Result<EmergencyContact> {
    if (shouldFail) return Result.failure(Exception("Add contact failed"))
    val newContact = EmergencyContact(
      id = "c-${System.currentTimeMillis()}",
      name = name,
      phone = phone,
      relationship = relationship,
      isPrimary = isPrimary,
    )
    val list = mockUser.emergencyContacts.toMutableList()
    if (isPrimary) {
      for (i in list.indices) {
        list[i] = list[i].copy(isPrimary = false)
      }
    }
    list.add(newContact)
    mockUser = mockUser.copy(emergencyContacts = list)
    return Result.success(newContact)
  }

  override suspend fun updateEmergencyContact(
    contactId: String,
    name: String?,
    phone: String?,
    relationship: String?,
    isPrimary: Boolean?,
  ): Result<EmergencyContact> {
    if (shouldFail) return Result.failure(Exception("Update contact failed"))
    val list = mockUser.emergencyContacts.map { c ->
      if (c.id == contactId) {
        c.copy(
          name = name ?: c.name,
          phone = phone ?: c.phone,
          relationship = relationship ?: c.relationship,
          isPrimary = isPrimary ?: c.isPrimary,
        )
      } else if (isPrimary == true) {
        c.copy(isPrimary = false)
      } else {
        c
      }
    }
    mockUser = mockUser.copy(emergencyContacts = list)
    val updated = list.find { it.id == contactId } ?: mockUser.emergencyContacts.first()
    return Result.success(updated)
  }

  override suspend fun deleteEmergencyContact(contactId: String): Result<Boolean> {
    if (shouldFail) return Result.failure(Exception("Delete contact failed"))
    mockUser = mockUser.copy(emergencyContacts = mockUser.emergencyContacts.filter { it.id != contactId })
    return Result.success(true)
  }

  override suspend fun updateLanguages(languages: List<String>): Result<UserResponse> {
    if (shouldFail) return Result.failure(Exception("Update languages failed"))
    mockUser = mockUser.copy(languages = languages)
    return Result.success(mockUser)
  }

  override fun getStoredBloodGroup(): String? = mockUser.bloodGroup
  override fun getStoredUserName(): String? = mockUser.name
  override fun getStoredUserEmail(): String? = mockUser.email
  override fun isAnonymous(): Boolean = false
}
