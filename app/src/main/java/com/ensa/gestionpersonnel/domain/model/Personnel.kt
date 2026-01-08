package com.ensa.gestionpersonnel.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Personnel(
    val id: Long,
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
    val photoUrl: String?,
    val typeEmploye: String,
    val dateRecrutementMinistere: String,
    val dateRecrutementENSA: String,
    val gradeActuel: String,
    val echelleActuelle: Int,
    val echelonActuel: Int,
    val soldeConges: Int,
    val estActif: Boolean
) {
    fun getNomComplet(): String = "$prenomFr $nomFr"
    
    fun getAnciennete(): Int {
        return try {
            val dateRecrutement = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(dateRecrutementMinistere)
            val diff = Date().time - (dateRecrutement?.time ?: 0)
            (diff / (1000L * 60 * 60 * 24 * 365)).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
