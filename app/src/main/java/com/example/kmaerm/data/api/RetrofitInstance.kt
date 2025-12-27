package com.example.kmaerm.data.api

import android.content.Context
import com.example.kmaerm.data.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            TokenDataStore(context).token.first()
        }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}

object RetrofitInstance {
//    private const val BASE_URL = "http://10.0.2.2:8080/" // Địa chỉ localhost cho Android Emulator
    private const val BASE_URL = "https://beamingly-unevoked-kinley.ngrok-free.dev"

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .apply {
                appContext?.let { context ->
                    addInterceptor(AuthInterceptor(context))
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val loaiTaiLieuApi: LoaiTaiLieuApiService by lazy { retrofit.create(LoaiTaiLieuApiService::class.java) }
    val hoSoApi: HoSoApiService by lazy { retrofit.create(HoSoApiService::class.java) }
    val giayPhepApi: GiayPhepApiService by lazy { retrofit.create(GiayPhepApiService::class.java) }
    val doanhNghiepApi: DoanhNghiepApiService by lazy { retrofit.create(DoanhNghiepApiService::class.java) }
    val demoApi: DemoApiService by lazy { retrofit.create(DemoApiService::class.java) }
}
