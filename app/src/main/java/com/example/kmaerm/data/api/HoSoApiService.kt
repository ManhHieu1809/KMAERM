package com.example.kmaerm.data.api

import com.example.kmaerm.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface HoSoApiService {
    @GET("/api/v1/ho-so")
    suspend fun getHoSoByDoanhNghiep(@Query("doanh_nghiep_id") doanhNghiepId: String): Response<HoSoListResponse>

    @GET("/api/v1/ho-so")
    suspend fun getAllHoSo(): Response<HoSoListResponse>

    @GET("/api/v1/ho-so/{id}")
    suspend fun getHoSoById(@Path("id") id: String): Response<HoSoDetailResponse>

    @POST("/api/v1/ho-so")
    suspend fun createHoSo(@Body request: CreateHoSoRequest): Response<CreateHoSoResponse>

    @PUT("/api/v1/ho-so/{id}")
    suspend fun updateHoSo(@Path("id") id: String, @Body request: UpdateHoSoRequest): Response<HoSo>

    @DELETE("/api/v1/ho-so/{id}")
    suspend fun deleteHoSo(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("/api/v1/tai-lieu/upload")
    suspend fun uploadTaiLieu(
        @Part("ho_so_tai_lieu_id") hoSoTaiLieuId: RequestBody,
        @Part("tieu_de") tieuDe: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<TaiLieu>

    @DELETE("/api/v1/tai-lieu/{id}")
    suspend fun deleteTaiLieu(@Path("id") id: String): Response<Unit>

    @GET("/api/v1/tai-lieu/download/{id}")
    suspend fun downloadTaiLieu(@Path("id") id: String): Response<ResponseBody>
}
