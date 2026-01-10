package com.ensa.gestionpersonnel.ui.avancement.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensa.gestionpersonnel.data.local.AvancementLocalStorage
import com.ensa.gestionpersonnel.domain.model.Avancement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AvancementListViewModel @Inject constructor(
    private val avancementStorage: AvancementLocalStorage
) : ViewModel() {

    private val _avancements = MutableLiveData<List<Avancement>>()
    val avancements: LiveData<List<Avancement>> = _avancements

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadAvancements() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) {
                avancementStorage.getAllAvancements()
            }
            _avancements.value = result.sortedByDescending { it.dateEffet }
            _isLoading.value = false
        }
    }

    fun deleteAvancement(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                avancementStorage.deleteAvancement(id)
            }
            loadAvancements()
        }
    }

    fun searchAvancements(query: String) {
        viewModelScope.launch {
            val allAvancements = withContext(Dispatchers.IO) {
                avancementStorage.getAllAvancements()
            }

            val filtered = allAvancements.filter {
                it.gradePrecedent.contains(query, ignoreCase = true) ||
                        it.gradeNouveau.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }

            _avancements.value = filtered.sortedByDescending { it.dateEffet }
        }
    }
}