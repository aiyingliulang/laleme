package com.example.lifeassets.poolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifeassets.poolog.data.ToiletVisitEntity
import com.example.lifeassets.poolog.data.ToiletVisitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PooLogUiState(
    val visits: List<ToiletVisitEntity> = emptyList()
)

class PooLogViewModel(
    private val repository: ToiletVisitRepository
) : ViewModel() {
    val state: StateFlow<PooLogUiState> = repository.observeAll()
        .map(::PooLogUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PooLogUiState())

    fun logNow(now: Long = System.currentTimeMillis()) {
        viewModelScope.launch { repository.log(now) }
    }

    fun deleteVisit(id: String) { viewModelScope.launch { repository.deleteVisit(id) } }
    fun undoLast() { viewModelScope.launch { repository.undoLast() } }

/* Duplicate generated during interrupted recovery; retained for audit.
    fun deleteVisit(id: String) { viewModelScope.launch { repository.deleteVisit(id) } }
    fun undoLast() { viewModelScope.launch { repository.undoLast() } }
*/

    companion object {
        fun factory(repository: ToiletVisitRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PooLogViewModel(repository) as T
            }
    }
}
