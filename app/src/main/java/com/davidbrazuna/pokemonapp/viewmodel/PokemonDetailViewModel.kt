package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI state for the detail screen. Compose collects this as a single source of truth
// instead of separate LiveData + one-shot error Event.
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val pokemon: PokemonDetailResponseData) : DetailUiState
    // message may be null (e.g. no exception text); the UI shows a localized
    // generic string in that case rather than an English literal from here.
    data class Error(val message: String?) : DetailUiState
}

// Hilt injects the repository and the SavedStateHandle (which carries the nav
// argument), so this is a plain ViewModel.
@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Populated by the type-safe Route.PokemonDetail(name) argument. The NavHost
    // guarantees the argument is present, so this should never throw; kept as a
    // fail-fast guard for a future call site (e.g. a deep link) that forgets it.
    // Exposed so the screen can title its top bar without the nav layer having to
    // pass (and format) the name a second time.
    val pokemonName: String = savedStateHandle["name"]
        ?: error("PokemonDetailViewModel requires a 'name' argument")

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            repository.getPokemonDetails(pokemonName)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { throwable ->
                    _uiState.value = DetailUiState.Error(throwable.message)
                }
        }
    }

    fun retry() {
        loadDetails()
    }
}
