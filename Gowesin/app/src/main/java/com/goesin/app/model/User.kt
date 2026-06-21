package com.goesin.app.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val token: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
    val token: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)