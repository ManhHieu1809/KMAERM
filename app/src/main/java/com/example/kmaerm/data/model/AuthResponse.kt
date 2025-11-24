package com.example.kmaerm.data.model

data class AuthResponse(
    val data: AuthData,
    val message: String
)

data class AuthData(
    val access_token: String,
    val user: User
)

data class User(
    val id: String,
    val email: String,
    val full_name: String,
    val role_id: String,
    val role_name: String, // "DOANH_NGHIEP" or "CAN_BO"
    val doanh_nghiep_id: String?
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

data class ChangePasswordResponse(
    val message: String
)
