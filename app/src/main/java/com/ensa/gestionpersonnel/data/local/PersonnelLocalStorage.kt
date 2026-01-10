package com.ensa.gestionpersonnel.data.local

import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import com.ensa.gestionpersonnel.domain.model.Personnel

/**
 * Stockage local temporaire pour les personnels (mode test sans backend)
 * Utilise une liste en mémoire pour simuler une base de données
 */
object PersonnelLocalStorage {
    private val personnelList = mutableListOf<Personnel>()
    private var nextId = 1L

    fun getAllPersonnel(): List<Personnel> {
        return personnelList.toList()
    }

    fun getPersonnelById(id: Long): Personnel? {
        return personnelList.find { it.id == id }
    }

    fun searchPersonnel(query: String): List<Personnel> {
        val lowerQuery = query.lowercase()
        return personnelList.filter {
            it.nomFr.lowercase().contains(lowerQuery) ||
                    it.prenomFr.lowercase().contains(lowerQuery) ||
                    it.ppr.contains(query) ||
                    it.cin.lowercase().contains(lowerQuery) ||
                    it.email.lowercase().contains(lowerQuery)
        }
    }

    fun createPersonnel(dto: PersonnelDto): Personnel {
        val personnel = Personnel(
            id = nextId++,
            ppr = dto.ppr,
            cin = dto.cin,
            nomFr = dto.nomFr,
            nomAr = dto.nomAr,
            prenomFr = dto.prenomFr,
            prenomAr = dto.prenomAr,
            email = dto.email,
            telephone = dto.telephone,
            dateNaissance = dto.dateNaissance,
            lieuNaissance = dto.lieuNaissance,
            sexe = dto.sexe,
            adresse = dto.adresse,
            photoUrl = dto.photoUrl,
            typeEmploye = dto.typeEmploye,
            dateRecrutementMinistere = dto.dateRecrutementMinistere,
            dateRecrutementENSA = dto.dateRecrutementENSA,
            gradeActuel = dto.gradeActuel,
            echelleActuelle = dto.echelleActuelle,
            echelonActuel = dto.echelonActuel,
            soldeConges = dto.soldeConges,
            estActif = dto.estActif
        )
        personnelList.add(personnel)
        return personnel
    }

    fun updatePersonnel(id: Long, dto: PersonnelDto): Personnel? {
        val index = personnelList.indexOfFirst { it.id == id }
        if (index == -1) return null

        val updatedPersonnel = Personnel(
            id = id,
            ppr = dto.ppr,
            cin = dto.cin,
            nomFr = dto.nomFr,
            nomAr = dto.nomAr,
            prenomFr = dto.prenomFr,
            prenomAr = dto.prenomAr,
            email = dto.email,
            telephone = dto.telephone,
            dateNaissance = dto.dateNaissance,
            lieuNaissance = dto.lieuNaissance,
            sexe = dto.sexe,
            adresse = dto.adresse,
            photoUrl = dto.photoUrl,
            typeEmploye = dto.typeEmploye,
            dateRecrutementMinistere = dto.dateRecrutementMinistere,
            dateRecrutementENSA = dto.dateRecrutementENSA,
            gradeActuel = dto.gradeActuel,
            echelleActuelle = dto.echelleActuelle,
            echelonActuel = dto.echelonActuel,
            soldeConges = dto.soldeConges,
            estActif = dto.estActif
        )
        personnelList[index] = updatedPersonnel
        return updatedPersonnel
    }

    // ✅ NOUVELLE MÉTHODE : Mettre à jour un personnel directement (pour déduction de congés)
    fun updatePersonnelDirect(personnel: Personnel): Personnel {
        val index = personnelList.indexOfFirst { it.id == personnel.id }
        if (index != -1) {
            personnelList[index] = personnel
        }
        return personnel
    }

    fun deletePersonnel(id: Long): Boolean {
        return personnelList.removeIf { it.id == id }
    }

    fun getStats(): Map<String, Any> {
        val total = personnelList.size
        val actifs = personnelList.count { it.estActif }
        val inactifs = total - actifs

        val repartitionParType = personnelList.groupBy { it.typeEmploye }
            .mapValues { it.value.size }

        val repartitionParGrade = personnelList.groupBy { it.gradeActuel }
            .mapValues { it.value.size }

        val moyenneAnciennete = if (personnelList.isNotEmpty()) {
            personnelList.map { it.getAnciennete() }.average().toInt()
        } else {
            0
        }

        val totalConges = personnelList.sumOf { it.soldeConges }

        return mapOf(
            "totalPersonnel" to total,
            "personnelActif" to actifs,
            "personnelInactif" to inactifs,
            "repartitionParType" to repartitionParType,
            "repartitionParGrade" to repartitionParGrade,
            "moyenneAnciennete" to moyenneAnciennete,
            "totalConges" to totalConges
        )
    }
}