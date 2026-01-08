package com.ensa.gestionpersonnel.data.remote.dto

data class PersonnelDto(
    val id: Long? = null,
    val ppr: String,
    val cin: String,
    val nomFr: String,
    val nomAr: String,
    val prenomFr: String,
    val prenomAr: String,
    val email: String,
    val telephone: String,
    val dateNaissance: String,
    val lieuNaissance: String,
    val sexe: String,
    val adresse: String,
    val photoUrl: String? = null,
    val typeEmploye: String,
    val dateRecrutementMinistere: String,
    val dateRecrutementENSA: String,
    val gradeActuel: String,
    val echelleActuelle: Int,
    val echelonActuel: Int,
    val soldeConges: Int = 30,
    val estActif: Boolean = true
)
