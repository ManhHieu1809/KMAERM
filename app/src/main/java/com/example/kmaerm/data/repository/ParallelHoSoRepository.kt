package com.example.kmaerm.data.repository

import com.example.kmaerm.core.coroutine.ParallelProcessor
import com.example.kmaerm.core.coroutine.partitionResults
import com.example.kmaerm.core.result.ApiResult
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repository for parallel processing of HoSo operations.
 * Uses ParallelProcessor for efficient batch operations.
 *
 * Example usage:
 * ```kotlin
 * val repository = ParallelHoSoRepository()
 * repository.fetchAllHoSoParallel().collect { result ->
 *     when (result) {
 *         is ApiResult.Loading -> showLoading()
 *         is ApiResult.Success -> updateUI(result.data)
 *         is ApiResult.Error -> showError(result.message)
 *     }
 * }
 * ```
 */
class ParallelHoSoRepository {

    private val processor = ParallelProcessor()
    private val hoSoApi = RetrofitInstance.hoSoApi

    /**
     * Fetch all HoSo across multiple pages in parallel.
     * First fetches page 0 to get total pages, then fetches remaining pages concurrently.
     *
     * @param doanhNghiepId Filter by doanh nghiep ID (optional, required for enterprise users)
     * @param pageSize Number of items per page (default: 20)
     * @return Flow emitting Loading -> Success/Error states with all HoSo
     *
     * Example:
     * ```kotlin
     * repository.fetchAllHoSoParallel(doanhNghiepId = "abc123").collect { result ->
     *     result.onSuccess { hoSoList ->
     *         println("Loaded ${hoSoList.size} hồ sơ")
     *     }
     * }
     * ```
     */
    fun fetchAllHoSoParallel(doanhNghiepId: String? = null, pageSize: Int = 20): Flow<ApiResult<List<HoSo>>> = flow {
        emit(ApiResult.Loading)

        try {
            // Fetch first page to get total count
            val firstPageResponse = if (doanhNghiepId != null) {
                hoSoApi.getHoSoByDoanhNghiep(doanhNghiepId)
            } else {
                hoSoApi.getAllHoSo()
            }

            if (!firstPageResponse.isSuccessful || firstPageResponse.body() == null) {
                emit(ApiResult.Error(
                    Exception("Failed to fetch first page: ${firstPageResponse.code()}"),
                    "Không thể tải trang đầu tiên (${firstPageResponse.code()})"
                ))
                return@flow
            }

            val firstPage = firstPageResponse.body()!!
            val total = firstPage.total
            val totalPages = (total + pageSize - 1) / pageSize

            // If only one page, return it
            if (totalPages <= 1) {
                emit(ApiResult.Success(firstPage.data))
                return@flow
            }

            // Fetch remaining pages in parallel (pages 1..totalPages-1)
            val remainingPagesResults = processor.batchFetchPages(
                totalPages = totalPages - 1,
                concurrency = 3
            ) { pageIndex ->
                // pageIndex is 0-based for remaining pages, so actual page is pageIndex + 1
                val response = if (doanhNghiepId != null) {
                    hoSoApi.getHoSoByDoanhNghiep(doanhNghiepId)
                } else {
                    hoSoApi.getAllHoSo()
                }
                response.body()?.data ?: emptyList()
            }

            // Combine first page with remaining pages
            val allHoSo = mutableListOf<HoSo>()
            allHoSo.addAll(firstPage.data)

            remainingPagesResults.forEach { result ->
                result.onSuccess { pageData ->
                    allHoSo.addAll(pageData)
                }
            }

            emit(ApiResult.Success(allHoSo))

        } catch (e: Exception) {
            emit(ApiResult.Error(e, "Lỗi khi tải danh sách hồ sơ: ${e.message}"))
        }
    }

    /**
     * Upload multiple documents in parallel with progress tracking.
     *
     * @param files List of files to upload
     * @param hoSoTaiLieuId ID of HoSoTaiLieu to upload to
     * @param onProgress Callback for upload progress (current, total)
     * @return Flow emitting Loading -> Success/Error with uploaded TaiLieu list
     *
     * Example:
     * ```kotlin
     * repository.uploadMultipleDocuments(
     *     files = listOf(file1, file2, file3),
     *     hoSoTaiLieuId = "123",
     *     onProgress = { current, total ->
     *         println("Uploaded $current/$total files")
     *     }
     * ).collect { result ->
     *     result.onSuccess { taiLieuList ->
     *         println("All files uploaded successfully")
     *     }
     * }
     * ```
     */
    fun uploadMultipleDocuments(
        files: List<File>,
        hoSoTaiLieuId: String,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Flow<ApiResult<List<TaiLieu>>> = flow {
        emit(ApiResult.Loading)

        try {
            if (files.isEmpty()) {
                emit(ApiResult.Success(emptyList()))
                return@flow
            }

            var completedCount = 0

            val results = processor.parallelUpload(
                files = files,
                concurrency = 3,
                onProgress = { _, _ ->
                    // Not using per-file progress, just tracking completion
                }
            ) { file, _ ->
                // Create multipart request
                val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestBody
                )

                val hoSoTaiLieuIdBody = hoSoTaiLieuId.toRequestBody("text/plain".toMediaTypeOrNull())
                val tieuDeBody = file.nameWithoutExtension.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = hoSoApi.uploadTaiLieu(
                    hoSoTaiLieuId = hoSoTaiLieuIdBody,
                    tieuDe = tieuDeBody,
                    file = filePart
                )

                if (response.isSuccessful && response.body() != null) {
                    completedCount++
                    onProgress(completedCount, files.size)
                    response.body()!!
                } else {
                    throw Exception("Upload failed for ${file.name}")
                }
            }

            val (successes, failures) = results.partitionResults()

            if (failures.isNotEmpty()) {
                val error = failures.first()
                emit(ApiResult.Error(
                    if (error is Exception) error else Exception(error),
                    "Tải lên ${successes.size}/${files.size} tệp thành công"
                ))
            } else {
                emit(ApiResult.Success(successes))
            }

        } catch (e: Exception) {
            emit(ApiResult.Error(e, "Lỗi khi tải lên tài liệu: ${e.message}"))
        }
    }

    /**
     * Batch update multiple HoSo in parallel.
     *
     * @param updates List of (hoSoId, UpdateHoSoRequest) pairs
     * @return Flow emitting Loading -> Success/Error with updated HoSo list
     *
     * Example:
     * ```kotlin
     * val updates = listOf(
     *     "id1" to UpdateHoSoRequest(trang_thai_ho_so = "DaDuyet"),
     *     "id2" to UpdateHoSoRequest(trang_thai_ho_so = "DaDuyet")
     * )
     * repository.batchUpdateHoSo(updates).collect { result ->
     *     result.onSuccess { hoSoList ->
     *         println("Updated ${hoSoList.size} hồ sơ")
     *     }
     * }
     * ```
     */
    fun batchUpdateHoSo(
        updates: List<Pair<String, UpdateHoSoRequest>>
    ): Flow<ApiResult<List<HoSo>>> = flow {
        emit(ApiResult.Loading)

        try {
            if (updates.isEmpty()) {
                emit(ApiResult.Success(emptyList()))
                return@flow
            }

            val results = processor.processBatch(
                items = updates,
                concurrency = 5
            ) { (hoSoId, request) ->
                val response = hoSoApi.updateHoSo(hoSoId, request)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    throw Exception("Failed to update HoSo $hoSoId (${response.code()}): $errorBody")
                }
            }

            val (successes, failures) = results.partitionResults()

            if (failures.isNotEmpty()) {
                val errorMessages = failures.mapNotNull { it.message }.take(3).joinToString("; ")
                emit(ApiResult.Error(
                    Exception(errorMessages),
                    "Cập nhật ${successes.size}/${updates.size} hồ sơ. Lỗi: $errorMessages"
                ))
            } else {
                emit(ApiResult.Success(successes))
            }

        } catch (e: Exception) {
            emit(ApiResult.Error(e, "Lỗi khi cập nhật hồ sơ: ${e.message}"))
        }
    }

    /**
     * Batch approve multiple HoSo in parallel.
     * Sets trang_thai_ho_so to "DaDuyet" for all specified HoSo.
     *
     * @param hoSoIds List of HoSo IDs to approve
     * @return Flow emitting Loading -> Success/Error with approved HoSo list
     *
     * Example:
     * ```kotlin
     * repository.batchApproveHoSo(listOf("id1", "id2", "id3")).collect { result ->
     *     result.onSuccess { approvedList ->
     *         println("Approved ${approvedList.size} hồ sơ")
     *     }
     * }
     * ```
     */
    fun batchApproveHoSo(hoSoIds: List<String>): Flow<ApiResult<List<HoSo>>> = flow {
        emit(ApiResult.Loading)

        try {
            if (hoSoIds.isEmpty()) {
                emit(ApiResult.Success(emptyList()))
                return@flow
            }

            val updates = hoSoIds.map { id ->
                id to UpdateHoSoRequest(trang_thai_ho_so = "DaDuyet")
            }

            // Reuse batchUpdateHoSo
            batchUpdateHoSo(updates).collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            emit(ApiResult.Error(e, "Lỗi khi phê duyệt hồ sơ: ${e.message}"))
        }
    }

    /**
     * Download multiple documents in parallel with retry mechanism.
     *
     * @param taiLieuList List of TaiLieu to download
     * @param outputDir Directory to save downloaded files
     * @return Flow emitting Loading -> Success/Error with downloaded File list
     *
     * Example:
     * ```kotlin
     * val outputDir = File(context.cacheDir, "downloads")
     * repository.downloadMultipleDocuments(
     *     taiLieuList = listOf(taiLieu1, taiLieu2),
     *     outputDir = outputDir
     * ).collect { result ->
     *     result.onSuccess { files ->
     *         println("Downloaded ${files.size} files to ${outputDir.path}")
     *     }
     * }
     * ```
     */
    fun downloadMultipleDocuments(
        taiLieuList: List<TaiLieu>,
        outputDir: File
    ): Flow<ApiResult<List<File>>> = flow {
        emit(ApiResult.Loading)

        try {
            if (taiLieuList.isEmpty()) {
                emit(ApiResult.Success(emptyList()))
                return@flow
            }

            // Create output directory if it doesn't exist
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val results = processor.parallelDownloadWithRetry(
                items = taiLieuList,
                concurrency = 3,
                maxRetry = 3
            ) { taiLieu ->
                val response = hoSoApi.downloadTaiLieu(taiLieu.id)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val fileName = "${taiLieu.tieu_de}.pdf"
                    val outputFile = File(outputDir, fileName)

                    FileOutputStream(outputFile).use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }

                    outputFile
                } else {
                    throw Exception("Failed to download ${taiLieu.tieu_de}")
                }
            }

            val (successes, failures) = results.partitionResults()

            if (failures.isNotEmpty()) {
                val error = failures.first()
                emit(ApiResult.Error(
                    if (error is Exception) error else Exception(error),
                    "Tải xuống ${successes.size}/${taiLieuList.size} tệp thành công"
                ))
            } else {
                emit(ApiResult.Success(successes))
            }

        } catch (e: Exception) {
            emit(ApiResult.Error(e, "Lỗi khi tải xuống tài liệu: ${e.message}"))
        }
    }
}

