package com.ensa.gestionpersonnel.data.remote.dto

data class PersonnelDto(
    val id: Long? = null,
    val nom: String,
    val prenom: String,
    val email: String? = null,
    val telephone: String? = null,
    val estActif: Boolean? = null
)
