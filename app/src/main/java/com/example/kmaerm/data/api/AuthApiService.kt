package com.example.kmaerm.data.api

import com.example.kmaerm.data.model.AuthResponse
import com.example.kmaerm.data.model.ChangePasswordRequest
import com.example.kmaerm.data.model.ChangePasswordResponse
import com.example.kmaerm.data.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/v1/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>
}
