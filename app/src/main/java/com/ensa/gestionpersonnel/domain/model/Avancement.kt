package com.ensa.gestionpersonnel.domain.model

import java.util.Date

/**
 * Modèle représentant un avancement de carrière d'un personnel
 */
data class Avancement(
    val id: Long = 0L,
    val dateDecision: Date,
    val personnelId: Long,  // ← AJOUT

    val dateEffet: Date,
    val gradePrecedent: String,
    val gradeNouveau: String,
    val echellePrecedente: Int,
    val echelleNouvelle: Int,
    val echelonPrecedent: Int,
    val echelonNouveau: Int,
    val description: String = ""
) {

    /**
     * Vérifie si l'avancement est valide
     * - La date d'effet doit être >= à la date de décision
     * - La nouvelle échelle/échelon doit être >= à l'ancienne
     */
    fun isValid(): Boolean {
        return dateEffet >= dateDecision &&
                (echelleNouvelle > echellePrecedente ||
                        (echelleNouvelle == echellePrecedente && echelonNouveau >= echelonPrecedent))
    }

    /**
     * Retourne un résumé lisible de l'avancement
     */
    fun getSummary(): String {
        return "$gradePrecedent (E$echellePrecedente-Ech$echelonPrecedent) → " +
                "$gradeNouveau (E$echelleNouvelle-Ech$echelonNouveau)"
    }

    /**
     * Calcule le nombre de jours entre la décision et l'effet
     */
    fun getDelaiEffet(): Int {
        val diff = dateEffet.time - dateDecision.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}