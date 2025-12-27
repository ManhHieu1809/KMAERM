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

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String,
    val data: OTPData? = null
)

data class OTPData(
    val otp_sent_at: Long,
    val expires_in: Int = 60
)

data class VerifyOTPRequest(
    val email: String,
    val otp: String
)

data class VerifyOTPResponse(
    val message: String,
    val data: OTPVerificationData? = null
)

data class OTPVerificationData(
    val verified: Boolean
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val new_password: String
)

data class ResetPasswordResponse(
    val message: String
)


data class BiometricSettingsRequest(
    val enabled: Boolean
)

data class BiometricSettingsResponse(
    val message: String,
    val data: BiometricData? = null
)

data class BiometricData(
    val biometric_enabled: Boolean,
    val updated_at: Long
)

