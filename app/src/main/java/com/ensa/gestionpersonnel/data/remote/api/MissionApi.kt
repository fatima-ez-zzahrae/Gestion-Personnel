package com.ensa.gestionpersonnel.data.remote.api

import com.ensa.gestionpersonnel.data.remote.dto.MissionDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface API pour la gestion des missions
 */
interface MissionApi {

    /**
     * Récupère toutes les missions
     */
    @GET("missions")
    suspend fun getAllMissions(): Response<List<MissionDto>>

    /**
     * Récupère les missions d'un personnel spécifique
     */
    @GET("missions/personnel/{personnelId}")
    suspend fun getMissionsByPersonnel(
        @Path("personnelId") personnelId: Long
    ): Response<List<MissionDto>>

    /**
     * Récupère les missions par statut
     */
    @GET("missions/statut/{statut}")
    suspend fun getMissionsByStatut(
        @Path("statut") statut: String
    ): Response<List<MissionDto>>

    /**
     * Récupère une mission par son ID
     */
    @GET("missions/{id}")
    suspend fun getMissionById(
        @Path("id") id: Long
    ): Response<MissionDto>

    /**
     * Crée une nouvelle mission
     */
    @POST("missions")
    suspend fun createMission(
        @Body mission: MissionDto
    ): Response<MissionDto>

    /**
     * Met à jour une mission existante
     */
    @PUT("missions/{id}")
    suspend fun updateMission(
        @Path("id") id: Long,
        @Body mission: MissionDto
    ): Response<MissionDto>

    /**
     * Clôture une mission (change son statut à TERMINEE)
     */
    @PATCH("missions/{id}/cloturer")
    suspend fun cloturerMission(
        @Path("id") id: Long
    ): Response<MissionDto>

    /**
     * Supprime une mission
     */
    @DELETE("missions/{id}")
    suspend fun deleteMission(
        @Path("id") id: Long
    ): Response<Unit>

    /**
     * Upload un rapport de mission
     */
    @Multipart
    @POST("missions/{id}/rapport")
    suspend fun uploadRapport(
        @Path("id") id: Long,
        @Part("rapport") rapportFile: okhttp3.MultipartBody.Part
    ): Response<MissionDto>
}