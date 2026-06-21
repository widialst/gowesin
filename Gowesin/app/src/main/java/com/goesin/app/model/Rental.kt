package com.goesin.app.model

import com.google.gson.annotations.SerializedName

data class Rental(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("bike_id") val bikeId: Int,
    @SerializedName("rental_date") val rentalDate: String,
    val duration: Int,
    @SerializedName("total_price") val totalPrice: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    val status: String,
    @SerializedName("bike_name") val bikeName: String? = null,
    @SerializedName("bike_image") val bikeImage: String? = null,
    @SerializedName("start_time") val startTime: String? = null
)

data class RentalResponse(
    val success: Boolean,
    val message: String,
    val data: List<Rental>?
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)