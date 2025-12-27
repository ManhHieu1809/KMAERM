package com.example.kmaerm.data.api

import com.example.kmaerm.data.model.DemoResult
import retrofit2.http.GET

interface DemoApiService {

    @GET("/api/v1/demo/sequential")
    suspend fun getSequentialResult(): DemoResult

    @GET("/api/v1/demo/parallel")
    suspend fun getParallelResult(): DemoResult
}

