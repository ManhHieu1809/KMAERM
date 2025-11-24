package com.example.kmaerm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.DoanhNghiep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfficerDoanhNghiepViewModel(application: Application) : AndroidViewModel(application) {

    private val _doanhNghiepList = MutableStateFlow<List<DoanhNghiep>>(emptyList())
    val doanhNghiepList: StateFlow<List<DoanhNghiep>> = _doanhNghiepList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total

    // Filter states
    private val _searchTenVi = MutableStateFlow("")
    val searchTenVi: StateFlow<String> = _searchTenVi

    private val _searchTenEn = MutableStateFlow("")
    val searchTenEn: StateFlow<String> = _searchTenEn

    private val _searchMaSo = MutableStateFlow("")
    val searchMaSo: StateFlow<String> = _searchMaSo

    init {
        loadDoanhNghiepList()
    }

    fun loadDoanhNghiepList(
        page: Int = 1,
        limit: Int = 10,
        tenVi: String? = null,
        tenEn: String? = null,
        maSo: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = RetrofitInstance.doanhNghiepApi.getAllDoanhNghiep(
                    page = page,
                    limit = limit,
                    tenVi = tenVi?.takeIf { it.isNotBlank() },
                    tenEn = tenEn?.takeIf { it.isNotBlank() },
                    maSo = maSo?.takeIf { it.isNotBlank() }
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _doanhNghiepList.value = body.data
                    _currentPage.value = body.page
                    _total.value = body.total
                    _totalPages.value = (body.total + limit - 1) / limit
                } else {
                    _error.value = "Không thể tải danh sách doanh nghiệp"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Lỗi kết nối"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchTenVi(value: String) {
        _searchTenVi.value = value
    }

    fun updateSearchTenEn(value: String) {
        _searchTenEn.value = value
    }

    fun updateSearchMaSo(value: String) {
        _searchMaSo.value = value
    }

    fun applyFilters() {
        loadDoanhNghiepList(
            page = 1,
            tenVi = _searchTenVi.value,
            tenEn = _searchTenEn.value,
            maSo = _searchMaSo.value
        )
    }

    fun clearFilters() {
        _searchTenVi.value = ""
        _searchTenEn.value = ""
        _searchMaSo.value = ""
        loadDoanhNghiepList()
    }

    fun loadNextPage() {
        if (_currentPage.value < _totalPages.value && !_isLoading.value) {
            loadDoanhNghiepList(
                page = _currentPage.value + 1,
                tenVi = _searchTenVi.value,
                tenEn = _searchTenEn.value,
                maSo = _searchMaSo.value
            )
        }
    }

    fun loadPreviousPage() {
        if (_currentPage.value > 1 && !_isLoading.value) {
            loadDoanhNghiepList(
                page = _currentPage.value - 1,
                tenVi = _searchTenVi.value,
                tenEn = _searchTenEn.value,
                maSo = _searchMaSo.value
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}

