package com.ensa.gestionpersonnel.data.local

import android.content.Context
import com.ensa.gestionpersonnel.domain.model.Avancement
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvancementLocalStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("avancements_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val KEY_AVANCEMENTS = "avancements_list"
        private const val KEY_COUNTER = "avancement_counter"
    }

    fun saveAvancement(avancement: Avancement): Avancement {
        val avancements = getAllAvancements().toMutableList()

        val savedAvancement = if (avancement.id == 0L) {
            val newId = getNextId()
            avancement.copy(id = newId)
        } else {
            avancements.removeIf { it.id == avancement.id }
            avancement
        }

        avancements.add(savedAvancement)
        saveAvancementsList(avancements)
        return savedAvancement
    }

    fun getAllAvancements(): List<Avancement> {
        val json = prefs.getString(KEY_AVANCEMENTS, null) ?: return emptyList()
        val type = object : TypeToken<List<AvancementData>>() {}.type
        val dataList: List<AvancementData> = gson.fromJson(json, type)
        return dataList.map { it.toAvancement() }
    }

    fun getAvancementById(id: Long): Avancement? {
        return getAllAvancements().find { it.id == id }
    }

    fun getAvancementsByPersonnelId(personnelId: Long): List<Avancement> {
        return getAllAvancements().filter { it.personnelId == personnelId }
            .sortedByDescending { it.dateEffet }
    }

    fun deleteAvancement(id: Long) {
        val avancements = getAllAvancements().toMutableList()
        avancements.removeIf { it.id == id }
        saveAvancementsList(avancements)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun saveAvancementsList(avancements: List<Avancement>) {
        val dataList = avancements.map { AvancementData.fromAvancement(it, dateFormat) }
        val json = gson.toJson(dataList)
        prefs.edit().putString(KEY_AVANCEMENTS, json).apply()
    }

    private fun getNextId(): Long {
        val currentId = prefs.getLong(KEY_COUNTER, 0L)
        val nextId = currentId + 1
        prefs.edit().putLong(KEY_COUNTER, nextId).apply()
        return nextId
    }

    private data class AvancementData(
        val id: Long,
        val personnelId: Long,
        val dateDecision: String,
        val dateEffet: String,
        val gradePrecedent: String,
        val gradeNouveau: String,
        val echellePrecedente: Int,
        val echelleNouvelle: Int,
        val echelonPrecedent: Int,
        val echelonNouveau: Int,
        val description: String
    ) {
        fun toAvancement(): Avancement {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return Avancement(
                id = id,
                personnelId = personnelId,
                dateDecision = dateFormat.parse(dateDecision) ?: Date(),
                dateEffet = dateFormat.parse(dateEffet) ?: Date(),
                gradePrecedent = gradePrecedent,
                gradeNouveau = gradeNouveau,
                echellePrecedente = echellePrecedente,
                echelleNouvelle = echelleNouvelle,
                echelonPrecedent = echelonPrecedent,
                echelonNouveau = echelonNouveau,
                description = description
            )
        }

        companion object {
            fun fromAvancement(avancement: Avancement, dateFormat: SimpleDateFormat): AvancementData {
                return AvancementData(
                    id = avancement.id,
                    personnelId = avancement.personnelId,
                    dateDecision = dateFormat.format(avancement.dateDecision),
                    dateEffet = dateFormat.format(avancement.dateEffet),
                    gradePrecedent = avancement.gradePrecedent,
                    gradeNouveau = avancement.gradeNouveau,
                    echellePrecedente = avancement.echellePrecedente,
                    echelleNouvelle = avancement.echelleNouvelle,
                    echelonPrecedent = avancement.echelonPrecedent,
                    echelonNouveau = avancement.echelonNouveau,
                    description = avancement.description
                )
            }
        }
    }
}