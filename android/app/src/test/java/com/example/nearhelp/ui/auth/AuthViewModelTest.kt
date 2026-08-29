package com.example.nearhelp.ui.auth

import com.example.nearhelp.data.api.models.MessageResponse
import com.example.nearhelp.data.api.models.TokenResponse
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.repository.IAuthRepository
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
class AuthViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeAuthRepository
  private lateinit var viewModel: AuthViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeAuthRepository()
    viewModel = AuthViewModel(fakeRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `login email validation flags invalid emails`() {
    viewModel.onLoginEmailChanged("invalid-email")
    assertNotNull(viewModel.loginForm.value.emailError)

    viewModel.onLoginEmailChanged("user@example.com")
    assertNull(viewModel.loginForm.value.emailError)
  }

  @Test
  fun `login password validation requires min 6 chars`() {
    viewModel.onLoginPasswordChanged("123")
    assertNotNull(viewModel.loginForm.value.passwordError)

    viewModel.onLoginPasswordChanged("securePassword123")
    assertNull(viewModel.loginForm.value.passwordError)
  }

  @Test
  fun `login success updates uiState to Success`() = runTest {
    viewModel.onLoginEmailChanged("test@nearhelp.org")
    viewModel.onLoginPasswordChanged("password123")
    viewModel.login()

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is AuthUiState.Success)
    val success = state as AuthUiState.Success
    assertEquals("Test User", success.user.name)
    assertEquals("test@nearhelp.org", success.user.email)
  }

  @Test
  fun `login failure updates uiState to Error`() = runTest {
    fakeRepository.shouldFail = true
    viewModel.onLoginEmailChanged("test@nearhelp.org")
    viewModel.onLoginPasswordChanged("wrongpassword")
    viewModel.login()

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is AuthUiState.Error)
  }

  @Test
  fun `sign up validation and successful registration`() = runTest {
    viewModel.onSignUpNameChanged("Dishari Ray")
    viewModel.onSignUpEmailChanged("dishari@nearhelp.org")
    viewModel.onSignUpPasswordChanged("securePassword123")
    viewModel.onSignUpBloodGroupSelected("O+")
    viewModel.onSignUpPhoneChanged("+919876543210")

    assertTrue(viewModel.signUpForm.value.isValid)

    viewModel.signUp()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is AuthUiState.Success)
  }

  @Test
  fun `anonymous emergency bypass activates emergency mode`() = runTest {
    viewModel.bypassAnonymousEmergency("Anonymous Hero")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is AuthUiState.Success)
    val success = state as AuthUiState.Success
    assertTrue(success.user.isAnonymous)
  }

  @Test
  fun `phone otp send and verify flow`() = runTest {
    viewModel.onOtpPhoneNumberChanged("+919876543210")
    viewModel.sendOtp()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is AuthUiState.OtpSent)

    viewModel.onOtpCodeChanged("123456")
    advanceUntilIdle()

    val verifiedState = viewModel.uiState.value
    assertTrue(verifiedState is AuthUiState.Success)
  }

  @Test
  fun `logout clears session state`() = runTest {
    viewModel.bypassAnonymousEmergency()
    advanceUntilIdle()

    assertTrue(fakeRepository.isLoggedIn())

    viewModel.logout()
    assertFalse(fakeRepository.isLoggedIn())
    assertEquals(AuthUiState.Idle, viewModel.uiState.value)
  }
}

private class FakeAuthRepository : IAuthRepository {
  var shouldFail: Boolean = false
  private var loggedIn: Boolean = false
  private var anonymous: Boolean = false
  private var userName: String? = null
  private var userEmail: String? = null

  override suspend fun register(
    email: String,
    password: String,
    name: String,
    phone: String?,
    bloodGroup: String?,
  ): Result<TokenResponse> {
    if (shouldFail) return Result.failure(Exception("Registration mock error"))
    loggedIn = true
    userName = name
    userEmail = email
    val user = UserResponse(
      id = "user-uuid-123",
      email = email,
      name = name,
      phone = phone,
      bloodGroup = bloodGroup,
      isAnonymous = false,
    )
    return Result.success(TokenResponse("access_mock", "refresh_mock", user = user))
  }

  override suspend fun login(email: String, password: String): Result<TokenResponse> {
    if (shouldFail) return Result.failure(Exception("Invalid credentials"))
    loggedIn = true
    userName = "Test User"
    userEmail = email
    val user = UserResponse(
      id = "user-uuid-123",
      email = email,
      name = "Test User",
      isAnonymous = false,
    )
    return Result.success(TokenResponse("access_mock", "refresh_mock", user = user))
  }

  override suspend fun loginWithGoogle(idToken: String): Result<TokenResponse> {
    if (shouldFail) return Result.failure(Exception("Google auth error"))
    loggedIn = true
    userName = "Google User"
    val user = UserResponse(
      id = "user-uuid-google",
      name = "Google User",
      authProvider = "google",
    )
    return Result.success(TokenResponse("access_mock", "refresh_mock", user = user))
  }

  override suspend fun sendPhoneOtp(phoneNumber: String): Result<MessageResponse> {
    if (shouldFail) return Result.failure(Exception("OTP send error"))
    return Result.success(MessageResponse("OTP sent to $phoneNumber", success = true))
  }

  override suspend fun verifyPhoneOtp(
    phoneNumber: String,
    otpCode: String,
    name: String?,
  ): Result<TokenResponse> {
    if (shouldFail || otpCode != "123456") return Result.failure(Exception("Invalid OTP"))
    loggedIn = true
    userName = name ?: "Phone User"
    val user = UserResponse(
      id = "user-uuid-phone",
      phone = phoneNumber,
      phoneVerified = true,
      name = userName,
      authProvider = "phone",
    )
    return Result.success(TokenResponse("access_mock", "refresh_mock", user = user))
  }

  override suspend fun createAnonymousEmergencySession(tempName: String?): Result<TokenResponse> {
    if (shouldFail) return Result.failure(Exception("Emergency bypass error"))
    loggedIn = true
    anonymous = true
    userName = tempName ?: "Anonymous Victim"
    val user = UserResponse(
      id = "anon-session-123",
      name = userName,
      isAnonymous = true,
      authProvider = "anonymous",
    )
    return Result.success(TokenResponse("access_mock", "refresh_mock", user = user))
  }

  override suspend fun fetchCurrentUser(): Result<UserResponse> {
    return Result.success(
      UserResponse(
        id = "user-uuid-123",
        name = userName,
        email = userEmail,
        isAnonymous = anonymous,
      )
    )
  }

  override fun isLoggedIn(): Boolean = loggedIn
  override fun isAnonymous(): Boolean = anonymous
  override fun getStoredUserId(): String? = if (loggedIn) "user-uuid-123" else null
  override fun getStoredUserName(): String? = userName
  override fun getStoredUserEmail(): String? = userEmail
  override fun getStoredBloodGroup(): String? = "O+"
  override fun getStoredAccessToken(): String? = if (loggedIn) "access_mock" else null
  override fun logout() {
    loggedIn = false
    anonymous = false
    userName = null
    userEmail = null
  }
}
