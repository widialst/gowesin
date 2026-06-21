package com.goesin.app.api

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // URL backend Next.js yang sudah di-deploy ke Vercel
    // Ganti dengan URL Vercel kamu setelah deploy
    private const val BASE_URL = "https://gowesin-backend-9s8i.vercel.app/api/"

    // Token disimpan di sini setelah login
    var authToken: String? = null

    val instance: ApiService by lazy {
        // OkHttp client dengan interceptor untuk inject JWT token otomatis
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original: Request = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")

                // Tambahkan Authorization header jika token tersedia
                authToken?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}