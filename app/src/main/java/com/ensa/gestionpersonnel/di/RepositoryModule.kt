package com.ensa.gestionpersonnel.di

import com.ensa.gestionpersonnel.data.local.PreferencesManager
import com.ensa.gestionpersonnel.data.remote.api.AuthApi
import com.ensa.gestionpersonnel.data.remote.api.PersonnelApi
import com.ensa.gestionpersonnel.data.repository.AuthRepository
import com.ensa.gestionpersonnel.data.repository.PersonnelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(authApi: AuthApi, preferencesManager: PreferencesManager): AuthRepository {
        return AuthRepository(authApi, preferencesManager)
    }

    @Provides
    @Singleton
    fun providePersonnelRepository(personnelApi: PersonnelApi): PersonnelRepository {
        return PersonnelRepository(personnelApi)
    }
}
