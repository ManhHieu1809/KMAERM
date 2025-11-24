package com.example.kmaerm.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class GiayPhepViewModel : ViewModel() {

    private val _giayPhepList = MutableStateFlow<List<GiayPhep>>(emptyList())
    val giayPhepList: StateFlow<List<GiayPhep>> = _giayPhepList

    private val _selectedGiayPhep = MutableStateFlow<GiayPhep?>(null)
    val selectedGiayPhep: StateFlow<GiayPhep?> = _selectedGiayPhep

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun loadGiayPhepList(doanhNghiepId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.giayPhepApi.getGiayPhepByDoanhNghiep(doanhNghiepId)
                if (response.isSuccessful && response.body() != null) {
                    _giayPhepList.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Không thể tải danh sách giấy phép"
                    _giayPhepList.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng."
                _giayPhepList.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGiayPhep(id: String, request: UpdateGiayPhepRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.giayPhepApi.updateGiayPhep(id, request)
                if (response.isSuccessful && response.body() != null) {
                    _successMessage.value = "Cập nhật giấy phép thành công"
                    _selectedGiayPhep.value = response.body()
                } else {
                    _error.value = "Không thể cập nhật giấy phép"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteGiayPhep(id: String, doanhNghiepId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.giayPhepApi.deleteGiayPhep(id)
                if (response.isSuccessful) {
                    _successMessage.value = "Xóa giấy phép thành công"
                    loadGiayPhepList(doanhNghiepId)
                } else {
                    _error.value = "Không thể xóa giấy phép"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadFile(id: String, file: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitInstance.giayPhepApi.uploadFile(id, filePart)
                if (response.isSuccessful && response.body() != null) {
                    _successMessage.value = "Upload file thành công"
                    _selectedGiayPhep.value = response.body()
                } else {
                    _error.value = "Không thể upload file"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi upload: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun viewFile(context: Context, id: String, fileName: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.giayPhepApi.viewFile(id)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val file = File(context.cacheDir, "view_license_${fileName}.pdf")
                    FileOutputStream(file).use { outputStream ->
                        body.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    try {
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        _error.value = "Không tìm thấy ứng dụng để mở file PDF"
                    }
                } else {
                    _error.value = "Không thể tải file"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải file: ${e.message}"
            }
        }
    }

    fun deleteFile(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.giayPhepApi.deleteGiayPhepFile(id)
                if (response.isSuccessful && response.body() != null) {
                    _successMessage.value = "Xóa file thành công"
                    _selectedGiayPhep.value = response.body()
                } else {
                    _error.value = "Không thể xóa file"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xóa file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pushToBlockchain(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.giayPhepApi.pushToBlockchain(id)
                if (response.isSuccessful && response.body() != null) {
                    _successMessage.value = "Đã đẩy lên blockchain thành công"
                    _selectedGiayPhep.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = when {
                        response.code() == 400 -> "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại thông tin."
                        response.code() == 404 -> "Không tìm thấy giấy phép"
                        response.code() == 500 -> "Lỗi server. Vui lòng thử lại sau."
                        errorBody != null -> errorBody
                        else -> "Không thể đẩy lên blockchain (Code: ${response.code()})"
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "Hết thời gian kết nối. Blockchain có thể đang xử lý, vui lòng kiểm tra lại sau."
            } catch (e: java.net.UnknownHostException) {
                _error.value = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng."
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message ?: "Không xác định"}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyBlockchain(id: String, request: VerifyBlockchainRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.giayPhepApi.verifyBlockchain(id, request)
                if (response.isSuccessful && response.body() != null) {
                    _successMessage.value = "Xác nhận blockchain thành công"
                    _selectedGiayPhep.value = response.body()
                } else {
                    _error.value = "Không thể xác nhận blockchain"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedGiayPhep(giayPhep: GiayPhep) {
        _selectedGiayPhep.value = giayPhep
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
