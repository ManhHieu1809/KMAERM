package com.example.kmaerm.data.repository

import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.data.model.DemoResult

class DemoRepository {

    private val demoApi = RetrofitInstance.demoApi

    suspend fun getSequentialResult(): DemoResult {
        return demoApi.getSequentialResult()
    }

    suspend fun getParallelResult(): DemoResult {
        return demoApi.getParallelResult()
    }
}

