package com.ensa.gestionpersonnel.domain.model

data class Personnel(
    val id: Long,
    val nom: String,
    val prenom: String,
    val email: String? = null,
    val telephone: String? = null,
    val estActif: Boolean = true
)
