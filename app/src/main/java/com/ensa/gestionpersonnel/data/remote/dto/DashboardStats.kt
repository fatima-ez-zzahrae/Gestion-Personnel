package com.ensa.gestionpersonnel.data.remote.dto

data class DashboardStats(
    val totalPersonnel: Int,
    val personnelActif: Int,
    val personnelInactif: Int,
    val absencesEnCours: Int,
    val missionsEnCours: Int,
    val avancementsAnnee: Int,
    val repartitionParType: Map<String, Int>, // Pédagogique, Administratif, Technique
    val evolutionRecrutement: List<MonthlyData>,
    val moyenneAnciennete: Int = 0,
    val totalConges: Int = 0
)

