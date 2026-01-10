package com.ensa.gestionpersonnel.ui.diplome.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.local.DiplomeLocalStorage
import com.ensa.gestionpersonnel.domain.model.Diplome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DiplomeListViewModel @Inject constructor(
    private val diplomeStorage: DiplomeLocalStorage
) : ViewModel() {

    private val _diplomes = MutableLiveData<List<Diplome>>()
    val diplomes: LiveData<List<Diplome>> = _diplomes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDiplomes() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) {
                diplomeStorage.getAllDiplomes()
            }
            _diplomes.value = result.sortedByDescending { it.dateObtention }
            _isLoading.value = false
        }
    }

    fun deleteDiplome(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                diplomeStorage.deleteDiplome(id)
            }
            loadDiplomes()
        }
    }

    fun searchDiplomes(query: String) {
        viewModelScope.launch {
            val allDiplomes = withContext(Dispatchers.IO) {
                diplomeStorage.getAllDiplomes()
            }

            val filtered = allDiplomes.filter {
                it.intitule.contains(query, ignoreCase = true) ||
                        it.specialite.contains(query, ignoreCase = true) ||
                        it.etablissement.contains(query, ignoreCase = true)
            }

            _diplomes.value = filtered.sortedByDescending { it.dateObtention }
        }
    }
}