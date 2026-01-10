package com.ensa.gestionpersonnel.domain.model

import java.util.Date

/**
 * Énumération des niveaux de diplômes
 */
enum class NiveauDiplome {
    BAC,
    BAC_PLUS_2,
    LICENCE,
    MASTER,
    INGENIEUR,
    DOCTORAT
}

/**
 * Modèle représentant un diplôme d'un personnel
 */
data class Diplome(
    val id: Long = 0L,
    val intitule: String,
    val personnelId: Long,  // ← AJOUT

    val specialite: String,
    val niveau: NiveauDiplome,
    val etablissement: String,
    val dateObtention: Date,
    val fichierPreuve: String = "" // Chemin vers le fichier justificatif
) {

    /**
     * Vérifie si le diplôme est valide
     * - L'intitulé ne doit pas être vide
     * - La date d'obtention ne doit pas être dans le futur
     */
    fun estValide(): Boolean {
        return intitule.isNotBlank() &&
                specialite.isNotBlank() &&
                etablissement.isNotBlank() &&
                dateObtention <= Date()
    }

    /**
     * Retourne le nom complet du diplôme
     */
    fun getNomComplet(): String {
        return "$intitule - $specialite ($niveau)"
    }

    /**
     * Calcule l'ancienneté du diplôme en années
     */
    fun getAncienneteAnnees(): Int {
        val diff = Date().time - dateObtention.time
        return (diff / (1000L * 60 * 60 * 24 * 365)).toInt()
    }

    /**
     * Vérifie si un fichier justificatif est attaché
     */
    fun hasPreuve(): Boolean = fichierPreuve.isNotBlank()

    /**
     * Retourne le niveau numérique (utile pour les comparaisons)
     */
    fun getNiveauNumerique(): Int {
        return when (niveau) {
            NiveauDiplome.BAC -> 1
            NiveauDiplome.BAC_PLUS_2 -> 2
            NiveauDiplome.LICENCE -> 3
            NiveauDiplome.MASTER -> 4
            NiveauDiplome.INGENIEUR -> 4
            NiveauDiplome.DOCTORAT -> 5
        }
    }
}