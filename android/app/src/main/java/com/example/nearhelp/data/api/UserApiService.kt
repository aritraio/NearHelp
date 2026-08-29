package com.example.nearhelp.data.api

import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.data.api.models.EmergencyContactCreateRequest
import com.example.nearhelp.data.api.models.EmergencyContactUpdateRequest
import com.example.nearhelp.data.api.models.LanguagePreferencesRequest
import com.example.nearhelp.data.api.models.MedicalIdResponse
import com.example.nearhelp.data.api.models.MedicalIdUpdateRequest
import com.example.nearhelp.data.api.models.MessageResponse
import com.example.nearhelp.data.api.models.SkillClaimRequest
import com.example.nearhelp.data.api.models.SkillVerificationResponse
import com.example.nearhelp.data.api.models.UserProfileUpdateRequest
import com.example.nearhelp.data.api.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {

  @GET("api/v1/users/me")
  suspend fun getCurrentUserProfile(
    @Header("Authorization") bearerToken: String,
  ): Response<UserResponse>

  @PUT("api/v1/users/me")
  suspend fun updateUserProfile(
    @Header("Authorization") bearerToken: String,
    @Body request: UserProfileUpdateRequest,
  ): Response<UserResponse>

  @GET("api/v1/users/me/medical")
  suspend fun getMedicalId(
    @Header("Authorization") bearerToken: String,
  ): Response<MedicalIdResponse>

  @PATCH("api/v1/users/me/medical")
  suspend fun patchMedicalId(
    @Header("Authorization") bearerToken: String,
    @Body request: MedicalIdUpdateRequest,
  ): Response<MedicalIdResponse>

  @PUT("api/v1/users/me/medical")
  suspend fun updateMedicalId(
    @Header("Authorization") bearerToken: String,
    @Body request: MedicalIdUpdateRequest,
  ): Response<MedicalIdResponse>

  @GET("api/v1/users/me/contacts")
  suspend fun listEmergencyContacts(
    @Header("Authorization") bearerToken: String,
  ): Response<List<EmergencyContact>>

  @POST("api/v1/users/me/contacts")
  suspend fun addEmergencyContact(
    @Header("Authorization") bearerToken: String,
    @Body request: EmergencyContactCreateRequest,
  ): Response<EmergencyContact>

  @PUT("api/v1/users/me/contacts/{contact_id}")
  suspend fun updateEmergencyContact(
    @Header("Authorization") bearerToken: String,
    @Path("contact_id") contactId: String,
    @Body request: EmergencyContactUpdateRequest,
  ): Response<EmergencyContact>

  @DELETE("api/v1/users/me/contacts/{contact_id}")
  suspend fun deleteEmergencyContact(
    @Header("Authorization") bearerToken: String,
    @Path("contact_id") contactId: String,
  ): Response<MessageResponse>

  @PUT("api/v1/users/me/languages")
  suspend fun updateLanguages(
    @Header("Authorization") bearerToken: String,
    @Body request: LanguagePreferencesRequest,
  ): Response<UserResponse>

  @POST("api/v1/users/me/skills")
  suspend fun claimSkill(
    @Header("Authorization") bearerToken: String,
    @Body request: SkillClaimRequest,
  ): Response<SkillVerificationResponse>

  @GET("api/v1/users/me/skills")
  suspend fun listMySkills(
    @Header("Authorization") bearerToken: String,
  ): Response<List<SkillVerificationResponse>>
}

