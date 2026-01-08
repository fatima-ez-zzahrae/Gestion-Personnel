package com.ensa.gestionpersonnel.domain.usecase

import com.ensa.gestionpersonnel.data.repository.PersonnelRepository
import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import com.ensa.gestionpersonnel.utils.NetworkResult
import javax.inject.Inject

class GetPersonnelListUseCase @Inject constructor(
    private val personnelRepository: PersonnelRepository
) {

    suspend operator fun invoke(): NetworkResult<List<PersonnelDto>> {
        return personnelRepository.getPersonnels()
    }
}
