package com.goesin.app.api

import com.goesin.app.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ── Auth ───────────────────────────────────────────────────────────────
    @POST("auth/login")
    fun login(
        @Body body: Map<String, String>
    ): Call<LoginResponse>

    @POST("auth/register")
    fun register(
        @Body body: Map<String, String>
    ): Call<RegisterResponse>

    // ── Bikes ──────────────────────────────────────────────────────────────
    @GET("bikes")
    fun getBikes(): Call<BikeResponse>

    // ── Rentals ────────────────────────────────────────────────────────────
    @POST("rentals")
    fun rentBike(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BaseResponse>

    @GET("rentals")
    fun getRentals(
        @Query("user_id") userId: Int
    ): Call<RentalResponse>

    @POST("rentals/{id}/return")
    fun returnBike(
        @Path("id") rentalId: Int
    ): Call<BaseResponse>
}