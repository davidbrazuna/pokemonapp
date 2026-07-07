package com.davidbrazuna.pokemonapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI state for the detail screen. Compose collects this as a single source of truth
// instead of separate LiveData + one-shot error Event.
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val pokemon: PokemonDetailResponseData) : DetailUiState
    // message may be null (e.g. no exception text); the UI shows a localized
    // generic string in that case rather than an English literal from here.
    data class Error(val message: String?) : DetailUiState
}

class PokemonDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Instantiated directly (not via constructor) because the default factory only
    // auto-injects Application/SavedStateHandle. The database is unused by detail
    // calls but the repository now requires it. Revisit once Hilt lands.
    private val repository = PokemonRepository(
        RetrofitInstance.api,
        PokemonDatabase.getInstance(application)
    )

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
