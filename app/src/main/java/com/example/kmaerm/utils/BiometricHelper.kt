package com.example.kmaerm.utils

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    enum class BiometricStatus {
        AVAILABLE,
        NONE_ENROLLED,       // Thiết bị hỗ trợ nhưng chưa đăng ký
        NO_HARDWARE,         // Thiết bị không có hardware biometric
        NOT_AVAILABLE        // Không khả dụng (vì lý do khác)
    }

    fun canUseBiometric(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)

        val strongResult = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
        println("🔐 [canUseBiometric] BIOMETRIC_STRONG result: ${biometricResultToString(strongResult)}")

        if (strongResult == BiometricManager.BIOMETRIC_SUCCESS) {
            return BiometricStatus.AVAILABLE
        }

        val weakResult = biometricManager.canAuthenticate(BIOMETRIC_WEAK)
        println("🔐 [canUseBiometric] BIOMETRIC_WEAK result: ${biometricResultToString(weakResult)}")

        if (weakResult == BiometricManager.BIOMETRIC_SUCCESS) {
            return BiometricStatus.AVAILABLE
        }

        val credentialResult = biometricManager.canAuthenticate(DEVICE_CREDENTIAL)
        println("🔐 [canUseBiometric] DEVICE_CREDENTIAL result: ${biometricResultToString(credentialResult)}")

        if (credentialResult == BiometricManager.BIOMETRIC_SUCCESS) {
            println("🔐 [canUseBiometric] Device credential (PIN/Pattern/Password) available")
            return BiometricStatus.AVAILABLE
        }

        val combinedResult = biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        println("🔐 [canUseBiometric] BIOMETRIC_WEAK | DEVICE_CREDENTIAL result: ${biometricResultToString(combinedResult)}")

        if (combinedResult == BiometricManager.BIOMETRIC_SUCCESS) {
            println("🔐 [canUseBiometric] Combined authentication available")
            return BiometricStatus.AVAILABLE
        }

        return when {
            strongResult == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ||
            weakResult == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricStatus.NONE_ENROLLED
            }
            strongResult == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
            weakResult == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE &&
            credentialResult != BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricStatus.NO_HARDWARE
            }
            else -> {
                println("🔐 [canUseBiometric] Returning NOT_AVAILABLE")
                BiometricStatus.NOT_AVAILABLE
            }
        }
    }

    private fun biometricResultToString(result: Int): String {
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> "BIOMETRIC_SUCCESS"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "BIOMETRIC_ERROR_NO_HARDWARE"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "BIOMETRIC_ERROR_HW_UNAVAILABLE"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "BIOMETRIC_ERROR_NONE_ENROLLED"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "BIOMETRIC_ERROR_UNSUPPORTED"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "BIOMETRIC_STATUS_UNKNOWN"
            else -> "UNKNOWN_ERROR ($result)"
        }
    }


    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String = "Sử dụng sinh trắc học để xác thực",
        description: String? = null,
        negativeButtonText: String = "Hủy",
        allowDeviceCredential: Boolean = true, // Mặc định CHO PHÉP PIN/Password fallback
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit,
        onFailed: () -> Unit
    ) {
        println("🔐 [BiometricHelper] showBiometricPrompt called")
        println("🔐 [BiometricHelper] Title: $title")
        println("🔐 [BiometricHelper] Activity: ${activity.javaClass.simpleName}")
        println("🔐 [BiometricHelper] allowDeviceCredential: $allowDeviceCredential")

        val biometricManager = BiometricManager.from(activity)

        // Kiểm tra xem có biometric hardware không
        val hasBiometricHardware = biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS ||
                                   biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

        println("🔐 [BiometricHelper] hasBiometricHardware: $hasBiometricHardware")

        val executor = ContextCompat.getMainExecutor(activity)

        println("🔐 [BiometricHelper] Creating BiometricPrompt...")

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    println("✅ [BiometricHelper] Authentication succeeded!")
                    println("✅ [BiometricHelper] Auth type: ${result.authenticationType}")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    println("❌ [BiometricHelper] Authentication error: code=$errorCode, msg=$errString")

                    // Xử lý các error codes phổ biến
                    val message = when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED -> "Đã hủy xác thực"
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "Đã hủy xác thực"
                        BiometricPrompt.ERROR_TIMEOUT -> "Hết thời gian xác thực"
                        BiometricPrompt.ERROR_LOCKOUT -> "Đã thử quá nhiều lần. Vui lòng thử lại sau."
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "Đã khóa. Vui lòng sử dụng mật khẩu."
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> "Chưa đăng ký sinh trắc học"
                        else -> errString.toString()
                    }

                    onError(errorCode, message)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    println("⚠️ [BiometricHelper] Authentication failed (wrong fingerprint/face)")
                    onFailed()
                }
            }
        )

        // Tạo PromptInfo tùy theo option
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .apply {
                description?.let { setDescription(it) }
            }

        val authenticators = when {
            allowDeviceCredential -> {
                if (hasBiometricHardware) {
                    BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
                } else {
                    DEVICE_CREDENTIAL
                }
            }
            hasBiometricHardware -> {
                BIOMETRIC_STRONG or BIOMETRIC_WEAK
            }
            else -> {
                println("🔐 [BiometricHelper] No biometric available, falling back to DEVICE_CREDENTIAL")
                DEVICE_CREDENTIAL
            }
        }

        println("🔐 [BiometricHelper] Using authenticators: $authenticators")
        promptInfoBuilder.setAllowedAuthenticators(authenticators)

        // Chỉ set negative button khi KHÔNG dùng DEVICE_CREDENTIAL
        if (authenticators and DEVICE_CREDENTIAL == 0) {
            promptInfoBuilder.setNegativeButtonText(negativeButtonText)
        }

        val promptInfo = promptInfoBuilder.build()

        println("🔐 [BiometricHelper] Calling biometricPrompt.authenticate()...")
        biometricPrompt.authenticate(promptInfo)
        println("🔐 [BiometricHelper] authenticate() called successfully")
    }

    fun getBiometricStatusMessage(context: Context): String {
        return when (canUseBiometric(context)) {
            BiometricStatus.AVAILABLE ->
                "Xác thực nhanh khả dụng"
            BiometricStatus.NONE_ENROLLED ->
                "Vui lòng đăng ký vân tay, FaceID hoặc PIN trong Settings điện thoại"
            BiometricStatus.NO_HARDWARE ->
                "Thiết bị không hỗ trợ xác thực nhanh"
            BiometricStatus.NOT_AVAILABLE ->
                "Xác thực nhanh không khả dụng"
        }
    }


    fun getBiometricTypeName(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ có thể detect type cụ thể
            "Vân tay / FaceID"
        } else {
            "Sinh trắc học"
        }
    }
}

