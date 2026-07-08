package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.AbilityItem
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

// UI state for the currently-selected ability's description. Separate from
// DetailUiState: the Pokemon itself only ever loads once, but the ability
// description reloads every time the selection changes.
sealed interface AbilityDescriptionUiState {
    data object Loading : AbilityDescriptionUiState
    data class Success(val description: String) : AbilityDescriptionUiState
    data class Error(val message: String?) : AbilityDescriptionUiState
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

    // There is always exactly one ability selected once the Pokemon loads (index
    // 0 initially) — never an empty/unselected state.
    private val _selectedAbilityIndex = MutableStateFlow(0)
    val selectedAbilityIndex: StateFlow<Int> = _selectedAbilityIndex.asStateFlow()

    private val _abilityDescriptionState =
        MutableStateFlow<AbilityDescriptionUiState>(AbilityDescriptionUiState.Loading)
    val abilityDescriptionState: StateFlow<AbilityDescriptionUiState> =
        _abilityDescriptionState.asStateFlow()

    // In-memory, on top of the repository's Room cache: avoids re-hitting even
    // Room for an ability already fetched this ViewModel lifetime (survives
    // rotation, since the ViewModel does). Room itself is what survives the app
    // being closed and reopened.
    private val descriptionCache = mutableMapOf<String, String>()

    // Cancelled and replaced on every selection change, so a slow response for
    // an ability the user has since navigated away from can't overwrite the
    // state of whatever is selected now.
    private var descriptionJob: Job? = null

    init {
        loadDetails()
    }

    private fun loadDetails() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            repository.getPokemonDetails(pokemonName)
                .onSuccess { pokemon ->
                    _uiState.value = DetailUiState.Success(pokemon)
                    _selectedAbilityIndex.value = 0
                    pokemon.abilities.firstOrNull()?.let { loadAbilityDescription(it.ability) }
                }
                .onFailure { throwable ->
                    _uiState.value = DetailUiState.Error(throwable.message)
                }
        }
    }

    fun selectAbility(index: Int) {
        val abilities = (_uiState.value as? DetailUiState.Success)?.pokemon?.abilities ?: return
        val ability = abilities.getOrNull(index) ?: return
        _selectedAbilityIndex.value = index
        loadAbilityDescription(ability.ability)
    }

    fun retryAbilityDescription() {
        val abilities = (_uiState.value as? DetailUiState.Success)?.pokemon?.abilities ?: return
        val ability = abilities.getOrNull(_selectedAbilityIndex.value)?.ability ?: return
        loadAbilityDescription(ability)
    }

    private fun loadAbilityDescription(ability: AbilityItem) {
        descriptionJob?.cancel()
        val cached = descriptionCache[ability.name]
        if (cached != null) {
            _abilityDescriptionState.value = AbilityDescriptionUiState.Success(cached)
            return
        }
        _abilityDescriptionState.value = AbilityDescriptionUiState.Loading
        descriptionJob = viewModelScope.launch {
            repository.getAbilityDescription(ability)
                .onSuccess { description ->
                    descriptionCache[ability.name] = description
                    _abilityDescriptionState.value = AbilityDescriptionUiState.Success(description)
                }
                .onFailure { throwable ->
                    _abilityDescriptionState.value = AbilityDescriptionUiState.Error(throwable.message)
                }
        }
    }

    fun retry() {
        loadDetails()
    }
}
