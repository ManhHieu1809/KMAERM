package com.example.kmaerm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfficerHoSoViewModel : ViewModel() {
    private val _hoSoList = MutableStateFlow<List<HoSo>>(emptyList())
    val hoSoList: StateFlow<List<HoSo>> = _hoSoList

    private val _hoSoDetail = MutableStateFlow<HoSo?>(null)
    val hoSoDetail: StateFlow<HoSo?> = _hoSoDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    fun loadAllHoSo() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = RetrofitInstance.hoSoApi.getAllHoSo()
                if (response.isSuccessful && response.body() != null) {
                    _hoSoList.value = response.body()!!.data
                } else {
                    _error.value = "Không thể tải danh sách hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHoSoDetail(hoSoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = RetrofitInstance.hoSoApi.getHoSoById(hoSoId)
                if (response.isSuccessful && response.body() != null) {
                    _hoSoDetail.value = response.body()!!.toHoSo()
                } else {
                    _error.value = "Không thể tải chi tiết hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateHoSo(hoSoId: String, request: UpdateHoSoRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _updateSuccess.value = false

                val response = RetrofitInstance.hoSoApi.updateHoSo(hoSoId, request)
                if (response.isSuccessful && response.body() != null) {
                    _hoSoDetail.value = response.body()!!
                    _updateSuccess.value = true
                } else {
                    _error.value = "Không thể cập nhật hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGiayPhep(request: CreateGiayPhepRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _updateSuccess.value = false

                // Bước 1: Tạo giấy phép
                val response = RetrofitInstance.giayPhepApi.createGiayPhep(request)
                if (response.isSuccessful && response.body() != null) {
                    // Bước 2: Sau khi tạo giấy phép thành công, TỰ ĐỘNG cập nhật hồ sơ sang "DaDuyet"
                    val hoSoId = request.ho_so_id
                    val currentHoSo = _hoSoDetail.value

                    if (currentHoSo != null) {
                        val updateRequest = UpdateHoSoRequest(
                            ngay_dang_ky = currentHoSo.ngay_dang_ky,
                            ngay_tiep_nhan = currentHoSo.ngay_tiep_nhan,
                            ngay_hen_tra = currentHoSo.ngay_hen_tra,
                            trang_thai_ho_so = "DaDuyet"  // Tự động chuyển sang Approved
                        )

                        val updateResponse = RetrofitInstance.hoSoApi.updateHoSo(hoSoId, updateRequest)
                        if (updateResponse.isSuccessful && updateResponse.body() != null) {
                            _hoSoDetail.value = updateResponse.body()!!
                            _updateSuccess.value = true
                        } else {
                            _error.value = "Tạo giấy phép thành công nhưng không thể cập nhật trạng thái hồ sơ"
                        }
                    } else {
                        _updateSuccess.value = true
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = errorBody ?: "Không thể tạo giấy phép. Vui lòng kiểm tra thông tin và thử lại."
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}
