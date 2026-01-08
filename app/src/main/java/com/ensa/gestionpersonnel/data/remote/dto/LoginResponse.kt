package com.ensa.gestionpersonnel.data.remote.dto

data class LoginResponse(
    val token: String,
    val userId: Long? = null
)
