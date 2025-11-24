package com.example.kmaerm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.datastore.TokenDataStore
import com.example.kmaerm.data.model.ChangePasswordRequest
import com.example.kmaerm.data.model.DoanhNghiep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenDataStore = TokenDataStore(application)

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _doanhNghiep = MutableStateFlow<DoanhNghiep?>(null)
    val doanhNghiep: StateFlow<DoanhNghiep?> = _doanhNghiep

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState

    sealed class ChangePasswordState {
        object Idle : ChangePasswordState()
        object Loading : ChangePasswordState()
        object Success : ChangePasswordState()
        data class Error(val message: String) : ChangePasswordState()
    }

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _fullName.value = tokenDataStore.fullName.first() ?: ""
            _email.value = tokenDataStore.email.first() ?: ""

            // Load thông tin doanh nghiệp
            val doanhNghiepId = tokenDataStore.doanhNghiepId.first()
            doanhNghiepId?.let {
                loadDoanhNghiep(it)
            }
        }
    }

    private fun loadDoanhNghiep(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.doanhNghiepApi.getDoanhNghiepById(id)

                if (response.isSuccessful && response.body() != null) {
                    _doanhNghiep.value = response.body()!!.data
                } else {
                    _error.value = "Không thể tải thông tin doanh nghiệp"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _changePasswordState.value = ChangePasswordState.Loading

                val request = ChangePasswordRequest(oldPassword, newPassword)
                val response = RetrofitInstance.authApi.changePassword(request)

                if (response.isSuccessful) {
                    _changePasswordState.value = ChangePasswordState.Success
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Đổi mật khẩu thất bại"
                    _changePasswordState.value = ChangePasswordState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error(
                    e.message ?: "Lỗi kết nối. Vui lòng thử lại."
                )
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }

    fun clearError() {
        _error.value = null
    }
}

