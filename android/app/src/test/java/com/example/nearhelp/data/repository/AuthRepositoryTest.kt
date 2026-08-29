package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.AuthApiService
import com.example.nearhelp.data.api.models.AnonymousAuthRequest
import com.example.nearhelp.data.api.models.GoogleAuthRequest
import com.example.nearhelp.data.api.models.LoginRequest
import com.example.nearhelp.data.api.models.MessageResponse
import com.example.nearhelp.data.api.models.PhoneSendOtpRequest
import com.example.nearhelp.data.api.models.PhoneVerifyRequest
import com.example.nearhelp.data.api.models.RegisterRequest
import com.example.nearhelp.data.api.models.TokenRefreshRequest
import com.example.nearhelp.data.api.models.TokenResponse
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.local.TokenStorage
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

  private lateinit var fakeApiService: FakeAuthApiService
  private lateinit var fakeStorage: FakeTokenStorage
  private lateinit var repository: AuthRepository

  @Before
  fun setUp() {
    fakeApiService = FakeAuthApiService()
    fakeStorage = FakeTokenStorage()
    // We can test AuthRepository with fake ApiService and test token storage
  }

  @Test
  fun `login returns success and persists tokens`() = runTest {
    val repo = AuthRepository(fakeApiService, fakeStorage)
    val result = repo.login("test@nearhelp.org", "password123")

    assertTrue(result.isSuccess)
    val tokenResponse = result.getOrNull()
    assertEquals("test_access_jwt", tokenResponse?.accessToken)
    assertEquals("test@nearhelp.org", fakeStorage.savedEmail)
  }

  @Test
  fun `register returns success and saves user profile`() = runTest {
    val repo = AuthRepository(fakeApiService, fakeStorage)
    val result = repo.register(
      email = "dishari@nearhelp.org",
      password = "securePassword123",
      name = "Dishari",
      bloodGroup = "O+",
    )

    assertTrue(result.isSuccess)
    assertEquals("Dishari", result.getOrNull()?.user?.name)
    assertEquals("O+", fakeStorage.savedBloodGroup)
  }

  @Test
  fun `anonymous emergency bypass returns anonymous token response`() = runTest {
    val repo = AuthRepository(fakeApiService, fakeStorage)
    val result = repo.createAnonymousEmergencySession("Anonymous Victim")

    assertTrue(result.isSuccess)
    val response = result.getOrNull()
    assertTrue(response?.user?.isAnonymous == true)
    assertTrue(fakeStorage.isAnon)
  }

  @Test
  fun `send phone otp returns success message`() = runTest {
    val repo = AuthRepository(fakeApiService, fakeStorage)
    val result = repo.sendPhoneOtp("+919876543210")

    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()?.success == true)
  }
}

class FakeTokenStorage : com.example.nearhelp.data.local.ITokenStorage {
  var savedAccessToken: String? = null
  var savedRefreshToken: String? = null
  var savedUserId: String? = null
  var savedUserName: String? = null
  var savedEmail: String? = null
  var savedBloodGroup: String? = null
  var isAnon: Boolean = false
  var fcmTokenVal: String? = null

  override fun saveSession(accessToken: String, refreshToken: String, user: UserResponse) {
    savedAccessToken = accessToken
    savedRefreshToken = refreshToken
    savedUserId = user.id
    savedUserName = user.name
    savedEmail = user.email
    savedBloodGroup = user.bloodGroup
    isAnon = user.isAnonymous
  }

  override fun getAccessToken(): String? = savedAccessToken
  override fun getRefreshToken(): String? = savedRefreshToken
  override fun getUserId(): String? = savedUserId
  override fun getUserName(): String? = savedUserName
  override fun getUserEmail(): String? = savedEmail
  override fun getUserPhone(): String? = null
  override fun getBloodGroup(): String? = savedBloodGroup
  override fun isAnonymous(): Boolean = isAnon
  override fun isLoggedIn(): Boolean = !savedAccessToken.isNullOrBlank()
  override fun saveFcmToken(token: String) { fcmTokenVal = token }
  override fun getFcmToken(): String? = fcmTokenVal
  override fun clear() {
    savedAccessToken = null
    savedRefreshToken = null
    savedUserId = null
    savedUserName = null
    savedEmail = null
    savedBloodGroup = null
    isAnon = false
  }
}

class FakeAuthApiService : AuthApiService {
  var shouldFail: Boolean = false

  override suspend fun register(request: RegisterRequest): Response<TokenResponse> {
    if (shouldFail) return Response.error(400, "User exists".toResponseBody("application/json".toMediaTypeOrNull()))
    val user = UserResponse(
      id = "user-123",
      email = request.email,
      name = request.name,
      bloodGroup = request.bloodGroup,
    )
    return Response.success(TokenResponse("test_access_jwt", "test_refresh_jwt", user = user))
  }

  override suspend fun login(request: LoginRequest): Response<TokenResponse> {
    if (shouldFail) return Response.error(401, "Invalid email/password".toResponseBody("application/json".toMediaTypeOrNull()))
    val user = UserResponse(
      id = "user-123",
      email = request.email,
      name = "Test User",
    )
    return Response.success(TokenResponse("test_access_jwt", "test_refresh_jwt", user = user))
  }

  override suspend fun googleAuth(request: GoogleAuthRequest): Response<TokenResponse> {
    val user = UserResponse(
      id = "user-google",
      name = "Google User",
      authProvider = "google",
    )
    return Response.success(TokenResponse("test_access_jwt", "test_refresh_jwt", user = user))
  }

  override suspend fun sendPhoneOtp(request: PhoneSendOtpRequest): Response<MessageResponse> {
    return Response.success(MessageResponse("OTP sent successfully", success = true))
  }

  override suspend fun verifyPhoneOtp(request: PhoneVerifyRequest): Response<TokenResponse> {
    val user = UserResponse(
      id = "user-phone",
      phone = request.phoneNumber,
      phoneVerified = true,
      authProvider = "phone",
    )
    return Response.success(TokenResponse("test_access_jwt", "test_refresh_jwt", user = user))
  }

  override suspend fun createAnonymousSession(request: AnonymousAuthRequest): Response<TokenResponse> {
    val user = UserResponse(
      id = "user-anon",
      name = request.tempName,
      isAnonymous = true,
      authProvider = "anonymous",
    )
    return Response.success(TokenResponse("test_access_jwt", "test_refresh_jwt", user = user))
  }

  override suspend fun refreshToken(request: TokenRefreshRequest): Response<TokenResponse> {
    val user = UserResponse(id = "user-123", name = "Test User")
    return Response.success(TokenResponse("new_access_jwt", "new_refresh_jwt", user = user))
  }

  override suspend fun registerDevice(bearerToken: String, request: com.example.nearhelp.data.api.models.DeviceRegisterRequest): Response<MessageResponse> {
    return Response.success(MessageResponse("Device registered", success = true))
  }

  override suspend fun getCurrentUser(bearerToken: String): Response<UserResponse> {
    return Response.success(UserResponse(id = "user-123", name = "Test User", email = "test@nearhelp.org"))
  }
}
