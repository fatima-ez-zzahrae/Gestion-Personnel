package com.ensa.gestionpersonnel.di

import android.content.Context
import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.data.remote.AuthInterceptor
import com.ensa.gestionpersonnel.data.remote.api.AbsenceApi
import com.ensa.gestionpersonnel.data.remote.api.AuthApi
import com.ensa.gestionpersonnel.data.remote.api.DashboardApi
import com.ensa.gestionpersonnel.data.remote.api.MissionApi  // ← AJOUTEZ CET IMPORT
import com.ensa.gestionpersonnel.data.remote.api.PersonnelApi
import com.ensa.gestionpersonnel.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferencesManager: PreferencesManager): AuthInterceptor {
        return AuthInterceptor(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun providePersonnelApi(retrofit: Retrofit): PersonnelApi {
        return retrofit.create(PersonnelApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAbsenceApi(retrofit: Retrofit): AbsenceApi {
        return retrofit.create(AbsenceApi::class.java)
    }

    // ← AJOUTEZ CETTE MÉTHODE
    @Provides
    @Singleton
    fun provideMissionApi(retrofit: Retrofit): MissionApi {
        return retrofit.create(MissionApi::class.java)
    }
}