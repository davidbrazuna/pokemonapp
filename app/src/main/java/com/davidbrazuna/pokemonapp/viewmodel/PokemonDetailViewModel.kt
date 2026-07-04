package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
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

    private val repository = PokemonRepository(RetrofitInstance.api)

    private val pokemonName: String = savedStateHandle[KEY_POKEMON_NAME]
        ?: error("PokemonDetailViewModel requires a '$KEY_POKEMON_NAME' argument")

    private val pokemonDetailsLiveData = MutableLiveData<PokemonDetailResponseData>()
    private val errorLiveData = MutableLiveData<String?>()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            repository.getPokemonDetails(pokemonName)
                .onSuccess {
                    pokemonDetailsLiveData.value = it
                    errorLiveData.value = null
                }
                .onFailure { throwable ->
                    errorLiveData.value = throwable.message ?: "Unknown error"
                }
        }
    }

    fun retry() {
        loadDetails()
    }

    fun observePokemonDetailsLiveData(): LiveData<PokemonDetailResponseData> =
        pokemonDetailsLiveData

    fun observeErrorLiveData(): LiveData<String?> = errorLiveData
}
