package com.ensa.gestionpersonnel.domain.model

data class Diplome(
    val id: Long,
    val intitule: String,
    val specialite: String? = null,
    val etablissement: String? = null
)
