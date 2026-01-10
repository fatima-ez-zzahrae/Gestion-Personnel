package com.ensa.gestionpersonnel.data.local

import android.content.Context
import com.ensa.gestionpersonnel.domain.model.Diplome
import com.ensa.gestionpersonnel.domain.model.NiveauDiplome
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiplomeLocalStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("diplomes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val KEY_DIPLOMES = "diplomes_list"
        private const val KEY_COUNTER = "diplome_counter"
    }

    fun saveDiplome(diplome: Diplome): Diplome {
        val diplomes = getAllDiplomes().toMutableList()

        val savedDiplome = if (diplome.id == 0L) {
            val newId = getNextId()
            diplome.copy(id = newId)
        } else {
            diplomes.removeIf { it.id == diplome.id }
            diplome
        }

        diplomes.add(savedDiplome)
        saveDiplomesList(diplomes)
        return savedDiplome
    }

    fun getAllDiplomes(): List<Diplome> {
        val json = prefs.getString(KEY_DIPLOMES, null) ?: return emptyList()
        val type = object : TypeToken<List<DiplomeData>>() {}.type
        val dataList: List<DiplomeData> = gson.fromJson(json, type)
        return dataList.map { it.toDiplome() }
    }

    fun getDiplomeById(id: Long): Diplome? {
        return getAllDiplomes().find { it.id == id }
    }

    fun getDiplomesByPersonnelId(personnelId: Long): List<Diplome> {
        return getAllDiplomes()
            .filter { it.personnelId == personnelId }
            .sortedByDescending { it.dateObtention }
    }

    fun deleteDiplome(id: Long) {
        val diplomes = getAllDiplomes().toMutableList()
        diplomes.removeIf { it.id == id }
        saveDiplomesList(diplomes)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun saveDiplomesList(diplomes: List<Diplome>) {
        val dataList = diplomes.map { DiplomeData.fromDiplome(it, dateFormat) }
        val json = gson.toJson(dataList)
        prefs.edit().putString(KEY_DIPLOMES, json).apply()
    }

    private fun getNextId(): Long {
        val currentId = prefs.getLong(KEY_COUNTER, 0L)
        val nextId = currentId + 1
        prefs.edit().putLong(KEY_COUNTER, nextId).apply()
        return nextId
    }

    private data class DiplomeData(
        val id: Long,
        val personnelId: Long,
        val intitule: String,
        val specialite: String,
        val niveau: String,
        val etablissement: String,
        val dateObtention: String,
        val fichierPreuve: String
    ) {
        fun toDiplome(): Diplome {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return Diplome(
                id = id,
                personnelId = personnelId,
                intitule = intitule,
                specialite = specialite,
                niveau = NiveauDiplome.valueOf(niveau),
                etablissement = etablissement,
                dateObtention = dateFormat.parse(dateObtention) ?: Date(),
                fichierPreuve = fichierPreuve
            )
        }

        companion object {
            fun fromDiplome(diplome: Diplome, dateFormat: SimpleDateFormat): DiplomeData {
                return DiplomeData(
                    id = diplome.id,
                    personnelId = diplome.personnelId,
                    intitule = diplome.intitule,
                    specialite = diplome.specialite,
                    niveau = diplome.niveau.name,
                    etablissement = diplome.etablissement,
                    dateObtention = dateFormat.format(diplome.dateObtention),
                    fichierPreuve = diplome.fichierPreuve
                )
            }
        }
    }
}