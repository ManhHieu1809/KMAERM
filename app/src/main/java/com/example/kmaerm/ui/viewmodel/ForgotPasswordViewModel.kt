package com.example.kmaerm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.data.model.ForgotPasswordRequest
import com.example.kmaerm.data.model.ResetPasswordRequest
import com.example.kmaerm.data.model.VerifyOTPRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenDataStore = TokenDataStore(application)

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private val _canResendOTP = MutableStateFlow(true)
    val canResendOTP: StateFlow<Boolean> = _canResendOTP.asStateFlow()

    private val _attemptCount = MutableStateFlow(0)
    val attemptCount: StateFlow<Int> = _attemptCount.asStateFlow()

    private var countdownJob: Job? = null
    private var currentEmail: String = ""
    private var currentOTP: String = ""

    companion object {
        const val OTP_EXPIRY_SECONDS = 60
        const val MAX_OTP_ATTEMPTS = 3
    }

    sealed class ForgotPasswordState {
        object Idle : ForgotPasswordState()
        object SendingOTP : ForgotPasswordState()
        data class OTPSent(val timestamp: Long) : ForgotPasswordState()
        object VerifyingOTP : ForgotPasswordState()
        object OTPVerified : ForgotPasswordState()
        object ResettingPassword : ForgotPasswordState()
        object Success : ForgotPasswordState()
        data class Error(val message: String) : ForgotPasswordState()
    }

    fun sendOTP(email: String) {
        if (email.isBlank()) {
            _state.value = ForgotPasswordState.Error("Vui lòng nhập email")
            return
        }

        if (!_canResendOTP.value) {
            _state.value = ForgotPasswordState.Error("Vui lòng đợi ${_remainingTime.value}s trước khi gửi lại")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = ForgotPasswordState.SendingOTP
                currentEmail = email

                val response = RetrofitInstance.authApi.sendOTP(ForgotPasswordRequest(email))

                if (response.isSuccessful && response.body() != null) {
                    val timestamp = System.currentTimeMillis()
                    tokenDataStore.saveOTPSentTimestamp(timestamp)
                    _state.value = ForgotPasswordState.OTPSent(timestamp)
                    _attemptCount.value = 0
                    startCountdown()
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    _state.value = ForgotPasswordState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _state.value = ForgotPasswordState.Error("Lỗi kết nối. Vui lòng thử lại.")
                e.printStackTrace()
            }
        }
    }

    fun verifyOTP(otp: String) {
        if (otp.length != 6) {
            _state.value = ForgotPasswordState.Error("OTP phải có 6 chữ số")
            return
        }

        if (_attemptCount.value >= MAX_OTP_ATTEMPTS) {
            _state.value = ForgotPasswordState.Error("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại OTP.")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = ForgotPasswordState.VerifyingOTP
                val response = RetrofitInstance.authApi.verifyOTP(VerifyOTPRequest(currentEmail, otp))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    val isVerified = body.data?.verified == true ||
                                   body.message.contains("hợp lệ", ignoreCase = true) ||
                                   body.message.contains("valid", ignoreCase = true)

                    if (isVerified) {
                        currentOTP = otp
                        _state.value = ForgotPasswordState.OTPVerified
                        stopCountdown()
                    } else {
                        _state.value = ForgotPasswordState.Error("OTP không hợp lệ")
                    }
                } else {
                    _attemptCount.value++
                    val remaining = MAX_OTP_ATTEMPTS - _attemptCount.value
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    _state.value = ForgotPasswordState.Error("$errorMsg Còn lại $remaining lần thử.")
                }
            } catch (e: Exception) {
                _state.value = ForgotPasswordState.Error("Lỗi kết nối. Vui lòng thử lại.")
                e.printStackTrace()
            }
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        when {
            newPassword.length < 6 -> {
                _state.value = ForgotPasswordState.Error("Mật khẩu phải có ít nhất 6 ký tự")
                return
            }
            newPassword != confirmPassword -> {
                _state.value = ForgotPasswordState.Error("Mật khẩu xác nhận không khớp")
                return
            }
        }

        viewModelScope.launch {
            try {
                _state.value = ForgotPasswordState.ResettingPassword
                val response = RetrofitInstance.authApi.resetPassword(
                    ResetPasswordRequest(currentEmail, currentOTP, newPassword)
                )

                if (response.isSuccessful) {
                    _state.value = ForgotPasswordState.Success
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    _state.value = ForgotPasswordState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _state.value = ForgotPasswordState.Error("Lỗi kết nối. Vui lòng thử lại.")
                e.printStackTrace()
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _canResendOTP.value = false
        _remainingTime.value = OTP_EXPIRY_SECONDS

        countdownJob = viewModelScope.launch {
            repeat(OTP_EXPIRY_SECONDS) {
                delay(1000)
                _remainingTime.value--
                if (_remainingTime.value <= 0) {
                    _canResendOTP.value = true
                }
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        _remainingTime.value = 0
        _canResendOTP.value = true
    }

    fun resetState() {
        _state.value = ForgotPasswordState.Idle
        _attemptCount.value = 0
        stopCountdown()
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
            json["message"]?.toString() ?: "Đã xảy ra lỗi"
        } catch (e: Exception) {
            "Đã xảy ra lỗi"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCountdown()
    }
}

