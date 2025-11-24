package com.example.kmaerm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.GiayPhep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfficerGiayPhepViewModel : ViewModel() {
    private val _giayPhepList = MutableStateFlow<List<GiayPhep>>(emptyList())
    val giayPhepList: StateFlow<List<GiayPhep>> = _giayPhepList

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
    private val _searchMaHoSo = MutableStateFlow("")
    val searchMaHoSo: StateFlow<String> = _searchMaHoSo

    private val _searchSoGiayPhep = MutableStateFlow("")
    val searchSoGiayPhep: StateFlow<String> = _searchSoGiayPhep

    private val _searchLoaiGiayPhep = MutableStateFlow("")
    val searchLoaiGiayPhep: StateFlow<String> = _searchLoaiGiayPhep

    private val _searchNgayHetHanFrom = MutableStateFlow("")
    val searchNgayHetHanFrom: StateFlow<String> = _searchNgayHetHanFrom

    private val _searchNgayHetHanTo = MutableStateFlow("")
    val searchNgayHetHanTo: StateFlow<String> = _searchNgayHetHanTo

    private val _searchDoanhNghiepId = MutableStateFlow("")
    val searchDoanhNghiepId: StateFlow<String> = _searchDoanhNghiepId

    init {
        loadAllGiayPhep()
    }

    fun loadAllGiayPhep(
        maHoSo: String? = null,
        soGiayPhep: String? = null,
        loaiGiayPhep: String? = null,
        ngayHetHanFrom: String? = null,
        ngayHetHanTo: String? = null,
        doanhNghiepId: String? = null,
        page: Int = 1,
        pageSize: Int = 50  // Tăng page size lên 50
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                println("=== DEBUG: Loading giay phep with params ===")
                println("maHoSo: $maHoSo, soGiayPhep: $soGiayPhep, loaiGiayPhep: $loaiGiayPhep")
                println("page: $page, pageSize: $pageSize")

                val response = RetrofitInstance.giayPhepApi.getAllGiayPhep(
                    maHoSo = if (maHoSo.isNullOrBlank()) null else maHoSo,
                    soGiayPhep = if (soGiayPhep.isNullOrBlank()) null else soGiayPhep,
                    loaiGiayPhep = if (loaiGiayPhep.isNullOrBlank()) null else loaiGiayPhep,
                    ngayHetHanFrom = if (ngayHetHanFrom.isNullOrBlank()) null else ngayHetHanFrom,
                    ngayHetHanTo = if (ngayHetHanTo.isNullOrBlank()) null else ngayHetHanTo,
                    doanhNghiepId = if (doanhNghiepId.isNullOrBlank()) null else doanhNghiepId,
                    page = page,
                    pageSize = pageSize
                )

                println("=== DEBUG: Response ===")
                println("isSuccessful: ${response.isSuccessful}")
                println("code: ${response.code()}")
                println("body: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _giayPhepList.value = body.data
                    _currentPage.value = body.page
                    _totalPages.value = if (body.total > 0) (body.total + pageSize - 1) / pageSize else 1
                    _total.value = body.total

                    println("=== DEBUG: Success ===")
                    println("Loaded ${body.data.size} giay phep")
                    println("Total: ${body.total}, Page: ${body.page}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("=== DEBUG: Error ===")
                    println("Error body: $errorBody")
                    _error.value = "Không thể tải danh sách giấy phép: ${response.code()}"
                }
            } catch (e: Exception) {
                println("=== DEBUG: Exception ===")
                println("Error: ${e.message}")
                e.printStackTrace()
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchMaHoSo(value: String) {
        _searchMaHoSo.value = value
    }

    fun updateSearchSoGiayPhep(value: String) {
        _searchSoGiayPhep.value = value
    }

    fun updateSearchLoaiGiayPhep(value: String) {
        _searchLoaiGiayPhep.value = value
    }

    fun updateSearchNgayHetHanFrom(value: String) {
        _searchNgayHetHanFrom.value = value
    }

    fun updateSearchNgayHetHanTo(value: String) {
        _searchNgayHetHanTo.value = value
    }

    fun updateSearchDoanhNghiepId(value: String) {
        _searchDoanhNghiepId.value = value
    }

    fun applyFilters() {
        loadAllGiayPhep(
            maHoSo = _searchMaHoSo.value,
            soGiayPhep = _searchSoGiayPhep.value,
            loaiGiayPhep = _searchLoaiGiayPhep.value,
            ngayHetHanFrom = _searchNgayHetHanFrom.value,
            ngayHetHanTo = _searchNgayHetHanTo.value,
            doanhNghiepId = _searchDoanhNghiepId.value,
            page = 1
        )
    }

    fun clearFilters() {
        _searchMaHoSo.value = ""
        _searchSoGiayPhep.value = ""
        _searchLoaiGiayPhep.value = ""
        _searchNgayHetHanFrom.value = ""
        _searchNgayHetHanTo.value = ""
        _searchDoanhNghiepId.value = ""
        loadAllGiayPhep(page = 1)
    }

    fun loadNextPage() {
        if (_currentPage.value < _totalPages.value) {
            loadAllGiayPhep(
                maHoSo = _searchMaHoSo.value,
                soGiayPhep = _searchSoGiayPhep.value,
                loaiGiayPhep = _searchLoaiGiayPhep.value,
                ngayHetHanFrom = _searchNgayHetHanFrom.value,
                ngayHetHanTo = _searchNgayHetHanTo.value,
                doanhNghiepId = _searchDoanhNghiepId.value,
                page = _currentPage.value + 1
            )
        }
    }

    fun loadPreviousPage() {
        if (_currentPage.value > 1) {
            loadAllGiayPhep(
                maHoSo = _searchMaHoSo.value,
                soGiayPhep = _searchSoGiayPhep.value,
                loaiGiayPhep = _searchLoaiGiayPhep.value,
                ngayHetHanFrom = _searchNgayHetHanFrom.value,
                ngayHetHanTo = _searchNgayHetHanTo.value,
                doanhNghiepId = _searchDoanhNghiepId.value,
                page = _currentPage.value - 1
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}
