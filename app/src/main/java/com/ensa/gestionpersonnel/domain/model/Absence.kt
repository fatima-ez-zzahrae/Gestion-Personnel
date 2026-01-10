package com.ensa.gestionpersonnel.domain.model

import java.util.Date

enum class AbsenceType {
    MALADIE,
    CONGE_ANNUEL,
    EXCEPTIONNELLE,
    NON_JUSTIFIEE
}

data class Absence(
    val id: Long? = null,
    val personnelId: Long,
    val personnelNom: String,
    val personnelPrenom: String,
    val personnelPpr: String,
    val dateDebut: Date,
    val dateFin: Date,
    val type: AbsenceType,
    val motif: String? = null,
    val justificatifUrl: String? = null,
    val estValideeParAdmin: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun getDureeJours(): Int {
        val diff = dateFin.time - dateDebut.time
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1
    }

    fun estJustifiee(): Boolean {
        return type != AbsenceType.NON_JUSTIFIEE && justificatifUrl != null
    }
}