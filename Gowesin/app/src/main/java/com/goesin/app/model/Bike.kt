package com.goesin.app.model

import com.google.gson.annotations.SerializedName

data class Bike(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("price_per_hour") val pricePerHour: Double,
    val location: String,
    val battery: Int,
    val rating: Float,
    @SerializedName("image_url") val imageUrl: String,
    val status: String,
    val stock: Int = 0,
    val lat: Double = -6.2088,
    val lng: Double = 106.8456
)

data class BikeResponse(
    val success: Boolean,
    val message: String,
    val data: List<Bike>?
)