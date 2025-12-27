package com.example.kmaerm.data.api

import com.example.kmaerm.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GiayPhepApiService {
    @GET("/api/v1/giay-phep")
    suspend fun getGiayPhepByDoanhNghiep(@Query("doanh_nghiep_id") doanhNghiepId: String): Response<GiayPhepListResponse>

    // API lấy tất cả giấy phép với filter cho cán bộ
    @GET("/api/v1/giay-phep")
    suspend fun getAllGiayPhep(
        @Query("ma_ho_so") maHoSo: String? = null,
        @Query("so_giay_phep") soGiayPhep: String? = null,
        @Query("loai_giay_phep") loaiGiayPhep: String? = null,
        @Query("ngay_het_han_from") ngayHetHanFrom: String? = null,
        @Query("ngay_het_han_to") ngayHetHanTo: String? = null,
        @Query("doanh_nghiep_id") doanhNghiepId: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<GiayPhepListResponse>

    @POST("/api/v1/giay-phep")
    suspend fun createGiayPhep(@Body request: CreateGiayPhepRequest): Response<CreateGiayPhepResponse>

    @PUT("/api/v1/giay-phep/{id}")
    suspend fun updateGiayPhep(@Path("id") id: String, @Body request: UpdateGiayPhepRequest): Response<GiayPhep>

    @DELETE("/api/v1/giay-phep/{id}")
    suspend fun deleteGiayPhep(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("/api/v1/giay-phep/{id}/upload")
    suspend fun uploadFile(@Path("id") id: String, @Part file: MultipartBody.Part): Response<GiayPhep>

    @GET("/api/v1/giay-phep/{id}/view-file")
    suspend fun viewFile(@Path("id") id: String): Response<ResponseBody>

    @DELETE("/api/v1/giay-phep/{id}")
    suspend fun deleteGiayPhepFile(@Path("id") id: String): Response<GiayPhep>

    @POST("/api/v1/giay-phep/{id}/ky-so")
    suspend fun signLicense(@Path("id") id: String): Response<GiayPhep>

    @POST("/api/v1/giay-phep/{id}/push-blockchain")
    suspend fun pushToBlockchain(@Path("id") id: String): Response<BlockchainPushResponse>

    @GET("/api/v1/giay-phep/{id}/verify")
    suspend fun verifyBlockchain(@Path("id") id: String): Response<BlockchainVerifyResponse>
}
