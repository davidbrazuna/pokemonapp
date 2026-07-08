package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.Ability
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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

    // Bumped by retryAbilityDescription() to force a re-fetch of the current
    // selection. combine() only re-runs when an upstream value actually
    // changes, and re-selecting the same ability is a no-op on a StateFlow —
    // this nonce is what makes "retry the one that's already selected" work.
    private val _retryNonce = MutableStateFlow(0)

    // Derived from uiState + selectedAbilityIndex instead of imperatively
    // pushed to from selectAbility(). flatMapLatest cancels the previous
    // ability's in-flight fetch automatically whenever the combined key
    // changes, so a slow response for an ability the user has since moved
    // away from can never overwrite a newer selection — no manual Job field,
    // no manual cancel() call to remember.
    @OptIn(ExperimentalCoroutinesApi::class)
    val abilityDescriptionState: StateFlow<AbilityDescriptionUiState> =
        combine(uiState, selectedAbilityIndex, _retryNonce) { state, index, _ ->
            (state as? DetailUiState.Success)?.pokemon?.abilities?.getOrNull(index)
        }.flatMapLatest { ability ->
            if (ability == null) flowOf(AbilityDescriptionUiState.Loading) else descriptionFlow(ability)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AbilityDescriptionUiState.Loading
        )

    // repository.getAbilityDescription() is itself cache-first (Room, persists
    // across app restarts), so there is no second, ViewModel-level cache here
    // — a single source of truth instead of two caches that could disagree.
    private fun descriptionFlow(ability: Ability) = flow {
        emit(AbilityDescriptionUiState.Loading)
        repository.getAbilityDescription(ability.ability)
            .onSuccess { emit(AbilityDescriptionUiState.Success(it)) }
            .onFailure { emit(AbilityDescriptionUiState.Error(it.message)) }
    }

    init {
        loadDetails()
    }

    private fun loadDetails() {
        _uiState.value = DetailUiState.Loading
        _selectedAbilityIndex.value = 0
        viewModelScope.launch {
            repository.getPokemonDetails(pokemonName)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { throwable -> _uiState.value = DetailUiState.Error(throwable.message) }
        }
    }

    fun selectAbility(index: Int) {
        // FilterChip fires onClick on a plain tap even when already selected
        // (Material3 doesn't suppress it); without this guard, re-tapping the
        // current chip would still be a no-op today (same index -> combine()
        // doesn't re-run), but the guard also avoids a pointless StateFlow write.
        if (index == _selectedAbilityIndex.value) return
        _selectedAbilityIndex.value = index
    }

    fun retryAbilityDescription() {
        _retryNonce.value++
    }

    fun retry() {
        loadDetails()
    }
}
