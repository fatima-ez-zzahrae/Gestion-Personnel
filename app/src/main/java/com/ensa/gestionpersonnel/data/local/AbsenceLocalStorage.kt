package com.ensa.gestionpersonnel.data.local

import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.domain.model.AbsenceType
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbsenceLocalStorage @Inject constructor() {
    private val absencesMap = mutableMapOf<Long, MutableList<Absence>>()
    private val idGenerator = AtomicLong(1L)

    suspend fun saveAbsences(absences: List<Absence>) {
        absences.forEach { absence ->
            saveAbsence(absence)
        }
    }

    suspend fun saveAbsence(absence: Absence): Absence {
        // Générer un ID si l'absence n'en a pas
        val absenceWithId = if (absence.id == null || absence.id == 0L) {
            absence.copy(id = idGenerator.getAndIncrement())
        } else {
            absence
        }

        val personnelAbsences = absencesMap.getOrPut(absenceWithId.personnelId) { mutableListOf() }

        // Supprimer l'ancienne version si elle existe
        personnelAbsences.removeAll { it.id == absenceWithId.id }

        // Ajouter la nouvelle version
        personnelAbsences.add(absenceWithId)

        return absenceWithId
    }

    suspend fun updateAbsence(absence: Absence): Absence {
        return saveAbsence(absence)
    }

    suspend fun deleteAbsence(absenceId: Long) {
        absencesMap.values.forEach { list ->
            list.removeAll { it.id == absenceId }
        }
    }

    suspend fun getAbsencesByPersonnel(personnelId: Long): List<Absence> {
        return absencesMap[personnelId] ?: emptyList()
    }

    suspend fun getAllAbsences(): List<Absence> {
        return absencesMap.values.flatten()
    }

    suspend fun getAbsenceById(id: Long): Absence? {
        return absencesMap.values.flatten().firstOrNull { it.id == id }
    }

    /**
     * Déduire les jours du solde de congés du personnel
     */
    suspend fun deduireConges(personnelId: Long, nombreJours: Int): Boolean {
        val personnel = PersonnelLocalStorage.getPersonnelById(personnelId) ?: return false

        if (personnel.soldeConges >= nombreJours) {
            val updatedPersonnel = personnel.copy(
                soldeConges = personnel.soldeConges - nombreJours
            )
            PersonnelLocalStorage.updatePersonnelDirect(updatedPersonnel)
            return true
        }
        return false
    }

    /**
     * Pénaliser le personnel pour absence non justifiée
     * Déduction de 2 jours de congé pour chaque jour d'absence non justifiée
     */
    suspend fun penaliserAbsenceNonJustifiee(personnelId: Long, nombreJours: Int): Boolean {
        val personnel = PersonnelLocalStorage.getPersonnelById(personnelId) ?: return false

        val penalite = nombreJours * 2 // 2 jours de congé déduits par jour d'absence
        val updatedPersonnel = personnel.copy(
            soldeConges = maxOf(0, personnel.soldeConges - penalite) // Ne pas aller en négatif
        )
        PersonnelLocalStorage.updatePersonnelDirect(updatedPersonnel)
        return true
    }

    /**
     * Restaurer le solde de congés (en cas d'annulation d'absence)
     */
    suspend fun restaurerConges(personnelId: Long, nombreJours: Int, type: AbsenceType) {
        val personnel = PersonnelLocalStorage.getPersonnelById(personnelId) ?: return

        val joursARestaurer = when (type) {
            AbsenceType.CONGE_ANNUEL -> nombreJours
            AbsenceType.NON_JUSTIFIEE -> nombreJours * 2 // Restaurer la pénalité
            else -> 0
        }

        if (joursARestaurer > 0) {
            val updatedPersonnel = personnel.copy(
                soldeConges = personnel.soldeConges + joursARestaurer
            )
            PersonnelLocalStorage.updatePersonnelDirect(updatedPersonnel)
        }
    }
}