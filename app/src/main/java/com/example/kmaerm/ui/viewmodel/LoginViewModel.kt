package com.example.kmaerm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.data.model.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenDataStore = TokenDataStore(application)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    sealed class LoginState {
        object Idle : LoginState()
        data class Success(val role: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginState.value = LoginState.Idle

                // Gọi API đăng nhập thật
                val response = RetrofitInstance.authApi.login(
                    LoginRequest(email, password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.data.access_token
                    val user = authResponse.data.user

                    // Xác định role để điều hướng
                    val role = when (user.role_name) {
                        "DOANH_NGHIEP" -> "DoanhNghiep"
                        "CAN_BO" -> "CanBo"
                        else -> "DoanhNghiep" // Default
                    }

                    // Lưu thông tin user vào DataStore
                    tokenDataStore.saveAuth(
                        token = token,
                        role = role,
                        userId = user.id,
                        email = user.email,
                        fullName = user.full_name,
                        doanhNghiepId = user.doanh_nghiep_id
                    )

                    _loginState.value = LoginState.Success(role)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Đăng nhập thất bại"
                    _loginState.value = LoginState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(
                    e.message ?: "Lỗi kết nối. Vui lòng thử lại."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
