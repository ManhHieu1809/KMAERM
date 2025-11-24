package com.example.kmaerm.data.api

import com.example.kmaerm.data.model.DoanhNghiepListResponse
import com.example.kmaerm.data.model.DoanhNghiepResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DoanhNghiepApiService {
    @GET("/api/v1/doanh-nghiep/{id}")
    suspend fun getDoanhNghiepById(@Path("id") id: String): Response<DoanhNghiepResponse>

    @GET("/api/v1/doanh-nghiep")
    suspend fun getAllDoanhNghiep(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("ten_vi") tenVi: String? = null,
        @Query("ten_en") tenEn: String? = null,
        @Query("ma_so") maSo: String? = null
    ): Response<DoanhNghiepListResponse>
}
