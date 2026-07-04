package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonRepository = PokemonRepository(RetrofitInstance.api)
) : ViewModel() {

    private val pokemonListLiveData = MutableLiveData<List<PokemonWithImage>>()
    private val errorLiveData = MutableLiveData<String?>()

    private var nextPageUrl: String? = null
    private var previousPageUrl: String? = null

    companion object {
        private const val INITIAL_URL =
            "https://pokeapi.co/api/v2/pokemon/?offset=0&limit=20"
    }

    init {
        loadPokemonList(INITIAL_URL)
    }

    private fun loadPokemonList(url: String) {
        viewModelScope.launch {
            repository.getPokemonPage(url)
                .onSuccess { page ->
                    nextPageUrl = page.nextUrl
                    previousPageUrl = page.previousUrl
                    pokemonListLiveData.value = page.pokemons
                    errorLiveData.value = null
                }
                .onFailure { throwable ->
                    errorLiveData.value = throwable.message ?: "Unknown error"
                }
        }
    }

    fun getNextPage() {
        nextPageUrl?.let { loadPokemonList(it) }
    }

    fun getPreviousPage() {
        previousPageUrl?.let { loadPokemonList(it) }
    }

    // Re-triggers the initial load, e.g. after an error state.
    fun retry() {
        loadPokemonList(INITIAL_URL)
    }

    fun observePokemonListLiveData(): LiveData<List<PokemonWithImage>> = pokemonListLiveData

    fun observeErrorLiveData(): LiveData<String?> = errorLiveData
}
