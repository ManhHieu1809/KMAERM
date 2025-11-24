package com.example.kmaerm.data.api

import com.example.kmaerm.data.model.LoaiTaiLieuResponse
import retrofit2.Response
import retrofit2.http.GET

interface LoaiTaiLieuApiService {
    @GET("/api/v1/loai-tai-lieu")
    suspend fun getLoaiTaiLieu(): Response<LoaiTaiLieuResponse>
}

