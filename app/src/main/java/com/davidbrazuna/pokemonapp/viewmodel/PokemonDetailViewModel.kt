package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
import com.davidbrazuna.pokemonapp.util.Event
import kotlinx.coroutines.launch

// Constructor takes only SavedStateHandle so the default ViewModel factory can
// build it and auto-populate the handle from the Intent extras.
class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        // Key used to pass the Pokemon name through the Intent extras / SavedStateHandle.
        const val KEY_POKEMON_NAME = "POKEMON_NAME"
    }

    // Instantiated directly (not received via constructor, unlike PokemonListViewModel)
    // because the default factory only auto-injects a lone SavedStateHandle parameter.
    // Revisit once Hilt/@HiltViewModel is introduced.
    private val repository = PokemonRepository(RetrofitInstance.api)

    // PokemonDetailsScreen checks the extra before this ViewModel is created, so this
    // should never actually throw today; kept as a fail-fast guard for future call
    // sites (e.g. a deep link) that might forget to pass the argument.
    private val pokemonName: String = savedStateHandle[KEY_POKEMON_NAME]
        ?: error("PokemonDetailViewModel requires a '$KEY_POKEMON_NAME' argument")

    private val pokemonDetailsLiveData = MutableLiveData<PokemonDetailResponseData>()
    private val errorLiveData = MutableLiveData<Event<String>>()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            repository.getPokemonDetails(pokemonName)
                .onSuccess { pokemonDetailsLiveData.value = it }
                .onFailure { throwable ->
                    errorLiveData.value = Event(throwable.message ?: "Unknown error")
                }
        }
    }

    fun retry() {
        loadDetails()
    }

    fun observePokemonDetailsLiveData(): LiveData<PokemonDetailResponseData> =
        pokemonDetailsLiveData

    fun observeErrorLiveData(): LiveData<Event<String>> = errorLiveData
}
