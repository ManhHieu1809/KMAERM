package com.example.kmaerm.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.core.result.ApiResult
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.*
import com.example.kmaerm.data.repository.ParallelHoSoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HoSoViewModel : ViewModel() {

    private val parallelRepository = ParallelHoSoRepository()

    private val _hoSoList = MutableStateFlow<List<HoSo>>(emptyList())
    val hoSoList: StateFlow<List<HoSo>> = _hoSoList

    private val _thuTucList = MutableStateFlow<List<ThuTuc>>(emptyList())
    val thuTucList: StateFlow<List<ThuTuc>> = _thuTucList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedHoSo = MutableStateFlow<HoSo?>(null)
    val selectedHoSo: StateFlow<HoSo?> = _selectedHoSo

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog

    private val _uploadProgress = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val uploadProgress: StateFlow<Map<String, Boolean>> = _uploadProgress

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // New StateFlows for parallel processing
    private val _batchUploadProgress = MutableStateFlow<Pair<Int, Int>>(0 to 0)
    val batchUploadProgress: StateFlow<Pair<Int, Int>> = _batchUploadProgress

    private val _parallelLoadingState = MutableStateFlow(false)
    val parallelLoadingState: StateFlow<Boolean> = _parallelLoadingState

    private val _batchOperationResult = MutableStateFlow<String?>(null)
    val batchOperationResult: StateFlow<String?> = _batchOperationResult

    fun loadHoSoList(doanhNghiepId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.hoSoApi.getHoSoByDoanhNghiep(doanhNghiepId)
                if (response.isSuccessful && response.body() != null) {
                    _hoSoList.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Không thể tải danh sách hồ sơ"
                    _hoSoList.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng."
                _hoSoList.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadThuTucList() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.loaiTaiLieuApi.getLoaiTaiLieu()
                if (response.isSuccessful && response.body() != null) {
                    _thuTucList.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Không thể tải danh sách thủ tục: ${e.message}"
            }
        }
    }

    fun loadHoSoDetail(hoSoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitInstance.hoSoApi.getHoSoById(hoSoId)
                if (response.isSuccessful && response.body() != null) {
                    _selectedHoSo.value = response.body()?.toHoSo()
                } else {
                    _error.value = "Không thể tải chi tiết hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createHoSo(doanhNghiepId: String, loaiThuTuc: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Format ngày giờ hiện tại theo ISO 8601
                val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())

                val request = CreateHoSoRequest(
                    doanh_nghiep_id = doanhNghiepId,
                    loai_thu_tuc = loaiThuTuc,
                    ngay_dang_ky = currentDate
                )
                val response = RetrofitInstance.hoSoApi.createHoSo(request)
                if (response.isSuccessful && response.body() != null) {
                    _showCreateDialog.value = false
                    loadHoSoList(doanhNghiepId)
                } else {
                    _error.value = "Không thể tạo hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadTaiLieu(hoSoTaiLieuId: String, file: File, tieuDe: String) {
        viewModelScope.launch {
            val uploadKey = "upload-$hoSoTaiLieuId"
            _uploadProgress.value = _uploadProgress.value + (uploadKey to true)
            try {
                val hoSoTaiLieuIdBody = hoSoTaiLieuId.toRequestBody("text/plain".toMediaTypeOrNull())
                val tieuDeBody = tieuDe.toRequestBody("text/plain".toMediaTypeOrNull())
                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = RetrofitInstance.hoSoApi.uploadTaiLieu(
                    hoSoTaiLieuId = hoSoTaiLieuIdBody,
                    tieuDe = tieuDeBody,
                    file = filePart
                )

                if (response.isSuccessful) {
                    _successMessage.value = "Upload tài liệu thành công"
                    // Reload hồ sơ để cập nhật danh sách tài liệu
                    _selectedHoSo.value?.let { hoSo ->
                        loadHoSoDetail(hoSo.id)
                    }
                } else {
                    _error.value = "Không thể upload tài liệu"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi upload: ${e.message}"
                e.printStackTrace()
            } finally {
                _uploadProgress.value = _uploadProgress.value - uploadKey
            }
        }
    }

    fun deleteTaiLieu(taiLieuId: String, hoSoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.hoSoApi.deleteTaiLieu(taiLieuId)
                if (response.isSuccessful) {
                    _successMessage.value = "Xóa tài liệu thành công"
                    loadHoSoDetail(hoSoId)
                } else {
                    _error.value = "Không thể xóa tài liệu"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xóa tài liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun viewTaiLieu(context: Context, taiLieuId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.hoSoApi.downloadTaiLieu(taiLieuId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Tạo file tạm để mở
                    val file = File(context.cacheDir, "temp_${taiLieuId}.pdf")
                    FileOutputStream(file).use { outputStream ->
                        body.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // Mở file bằng Intent
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
                        _successMessage.value = "Không tìm thấy ứng dụng để mở file PDF"
                    }
                } else {
                    _error.value = "Không thể tải tài liệu"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải tài liệu: ${e.message}"
            }
        }
    }

    fun downloadTaiLieu(context: Context, taiLieuId: String, tieuDe: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.hoSoApi.downloadTaiLieu(taiLieuId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Tạo file tạm để mở
                    val file = File(context.cacheDir, "view_${taiLieuId}.pdf")
                    FileOutputStream(file).use { outputStream ->
                        body.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // Mở file bằng Intent
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
                    _error.value = "Không thể xem tài liệu"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xem tài liệu: ${e.message}"
            }
        }
    }

    fun updateHoSo(
        hoSoId: String,
        ngayDangKy: String? = null,
        ngayTiepNhan: String? = null,
        ngayHenTra: String? = null,
        trangThaiHoSo: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateHoSoRequest(
                    ngay_dang_ky = ngayDangKy,
                    ngay_tiep_nhan = ngayTiepNhan,
                    ngay_hen_tra = ngayHenTra,
                    trang_thai_ho_so = trangThaiHoSo
                )
                val response = RetrofitInstance.hoSoApi.updateHoSo(hoSoId, request)
                if (response.isSuccessful) {
                    _successMessage.value = "Cập nhật hồ sơ thành công"
                    loadHoSoDetail(hoSoId)
                } else {
                    _error.value = "Không thể cập nhật hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHoSo(hoSoId: String, doanhNghiepId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.hoSoApi.deleteHoSo(hoSoId)
                if (response.isSuccessful) {
                    _successMessage.value = "Xóa hồ sơ thành công"
                    loadHoSoList(doanhNghiepId)
                } else {
                    _error.value = "Không thể xóa hồ sơ"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xóa hồ sơ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showCreateDialog() {
        _showCreateDialog.value = true
        loadThuTucList()
    }

    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // ==================== PARALLEL PROCESSING FUNCTIONS ====================

    /**
     * Load all HoSo using parallel processing.
     * Fetches data using the same API as loadHoSoList for consistency.
     *
     * @param doanhNghiepId Filter by doanh nghiep ID (required for enterprise users)
     */
    fun loadAllHoSoParallel(doanhNghiepId: String? = null) {
        viewModelScope.launch {
            _parallelLoadingState.value = true
            _error.value = null

            try {
                val response = if (doanhNghiepId != null) {
                    RetrofitInstance.hoSoApi.getHoSoByDoanhNghiep(doanhNghiepId)
                } else {
                    RetrofitInstance.hoSoApi.getAllHoSo()
                }

                if (response.isSuccessful && response.body() != null) {
                    _hoSoList.value = response.body()?.data ?: emptyList()
                    _batchOperationResult.value = "Đã tải ${_hoSoList.value.size} hồ sơ thành công"
                } else {
                    _error.value = "Không thể tải danh sách hồ sơ (${response.code()})"
                    _hoSoList.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải hồ sơ: ${e.message}"
                e.printStackTrace()
            } finally {
                _parallelLoadingState.value = false
            }
        }
    }

    /**
     * Upload multiple files in parallel with progress tracking.
     *
     * @param files List of files to upload
     * @param hoSoTaiLieuId ID of HoSoTaiLieu to upload to
     */
    fun uploadMultipleFiles(files: List<File>, hoSoTaiLieuId: String) {
        if (files.isEmpty()) {
            _error.value = "Không có tệp nào để tải lên"
            return
        }

        viewModelScope.launch {
            _parallelLoadingState.value = true
            _batchUploadProgress.value = 0 to files.size
            _error.value = null

            try {
                parallelRepository.uploadMultipleDocuments(
                    files = files,
                    hoSoTaiLieuId = hoSoTaiLieuId,
                    onProgress = { current, total ->
                        _batchUploadProgress.value = current to total
                    }
                ).collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _parallelLoadingState.value = true
                        }
                        is ApiResult.Success -> {
                            _parallelLoadingState.value = false
                            _batchOperationResult.value = "Đã tải lên ${result.data.size} tệp thành công"
                            _successMessage.value = "Upload thành công ${result.data.size}/${files.size} tệp"

                            // Reload hồ sơ detail
                            _selectedHoSo.value?.let { hoSo ->
                                loadHoSoDetail(hoSo.id)
                            }
                        }
                        is ApiResult.Error -> {
                            _error.value = result.message ?: result.exception.message
                            _parallelLoadingState.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải lên tệp: ${e.message}"
                _parallelLoadingState.value = false
            } finally {
                _batchUploadProgress.value = 0 to 0
            }
        }
    }

    /**
     * Batch approve multiple HoSo in parallel.
     *
     * @param hoSoIds List of HoSo IDs to approve
     */
    fun batchApproveHoSo(hoSoIds: List<String>) {
        if (hoSoIds.isEmpty()) {
            _error.value = "Không có hồ sơ nào được chọn"
            return
        }

        viewModelScope.launch {
            _parallelLoadingState.value = true
            _error.value = null
            _batchUploadProgress.value = 0 to hoSoIds.size

            try {
                parallelRepository.batchApproveHoSo(hoSoIds).collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _parallelLoadingState.value = true
                        }
                        is ApiResult.Success -> {
                            _parallelLoadingState.value = false
                            _batchUploadProgress.value = result.data.size to hoSoIds.size
                            _batchOperationResult.value = "Đã duyệt ${result.data.size}/${hoSoIds.size} hồ sơ thành công"
                            _successMessage.value = "Phê duyệt thành công ${result.data.size} hồ sơ"

                            // Refresh danh sách
                            val currentList = _hoSoList.value
                            if (currentList.isNotEmpty()) {
                                val doanhNghiepId = currentList.firstOrNull()?.doanh_nghiep_id
                                doanhNghiepId?.let { loadHoSoList(it) }
                            }
                        }
                        is ApiResult.Error -> {
                            _parallelLoadingState.value = false
                            // Hiển thị message chi tiết từ error
                            val errorMsg = result.message ?: result.exception.message ?: "Lỗi không xác định"
                            _error.value = errorMsg
                            _batchOperationResult.value = errorMsg
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi phê duyệt hồ sơ: ${e.message}"
                _parallelLoadingState.value = false
            }
        }
    }

    /**
     * Download multiple documents in parallel.
     *
     * @param taiLieuList List of TaiLieu to download
     * @param context Android context for file operations
     */
    fun downloadMultipleDocuments(taiLieuList: List<TaiLieu>, context: Context) {
        if (taiLieuList.isEmpty()) {
            _error.value = "Không có tài liệu nào để tải xuống"
            return
        }

        viewModelScope.launch {
            _parallelLoadingState.value = true
            _error.value = null

            try {
                // Create output directory in cache
                val outputDir = File(context.cacheDir, "downloads")

                parallelRepository.downloadMultipleDocuments(
                    taiLieuList = taiLieuList,
                    outputDir = outputDir
                ).collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _parallelLoadingState.value = true
                        }
                        is ApiResult.Success -> {
                            _parallelLoadingState.value = false
                            _batchOperationResult.value = "Đã tải xuống ${result.data.size}/${taiLieuList.size} tệp"
                            _successMessage.value = "Download thành công ${result.data.size} tệp vào ${outputDir.path}"

                            // Open folder (optional)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(
                                        androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            outputDir
                                        ),
                                        "resource/folder"
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore if can't open folder
                            }
                        }
                        is ApiResult.Error -> {
                            _error.value = result.message ?: result.exception.message
                            _parallelLoadingState.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải xuống tài liệu: ${e.message}"
                _parallelLoadingState.value = false
            }
        }
    }

    /**
     * Clear batch operation result message.
     */
    fun clearBatchOperationResult() {
        _batchOperationResult.value = null
    }
}
