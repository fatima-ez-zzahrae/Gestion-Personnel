package com.ensa.gestionpersonnel.domain.model

data class ResponsableRH(
    val id: Long,
    val nom: String,
    val prenom: String,
    val email: String? = null,
    val username: String? = null
)
