package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
import com.davidbrazuna.pokemonapp.util.Event
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonRepository = PokemonRepository(RetrofitInstance.api)
) : ViewModel() {

    private val pokemonListLiveData = MutableLiveData<List<PokemonWithImage>>()
    private val errorLiveData = MutableLiveData<Event<String>>()

    private var nextPageUrl: String? = null
    private var previousPageUrl: String? = null
    private var lastRequestedUrl: String = INITIAL_URL
    private var isLoading = false

    companion object {
        // Relative to Retrofit's baseUrl (see RetrofitInstance.BASE_URL) so the
        // host isn't duplicated across files.
        private const val INITIAL_URL = "pokemon?offset=0&limit=20"
    }

    init {
        loadPokemonList(INITIAL_URL)
    }

    private fun loadPokemonList(url: String) {
        // Guards against overlapping requests (e.g. fast fling triggering
        // onScrolled multiple times) racing and corrupting pagination state.
        if (isLoading) return
        isLoading = true
        lastRequestedUrl = url

        viewModelScope.launch {
            try {
                repository.getPokemonPage(url)
                    .onSuccess { page ->
                        nextPageUrl = page.nextUrl
                        previousPageUrl = page.previousUrl
                        pokemonListLiveData.value = page.pokemons
                    }
                    .onFailure { throwable ->
                        errorLiveData.value = Event(throwable.message ?: "Unknown error")
                    }
            } finally {
                isLoading = false
            }
        }
    }

    fun getNextPage() {
        nextPageUrl?.let { loadPokemonList(it) }
    }

    fun getPreviousPage() {
        previousPageUrl?.let { loadPokemonList(it) }
    }

    // Re-triggers the request that failed, not necessarily the first page.
    fun retry() {
        loadPokemonList(lastRequestedUrl)
    }

    fun observePokemonListLiveData(): LiveData<List<PokemonWithImage>> = pokemonListLiveData

    fun observeErrorLiveData(): LiveData<Event<String>> = errorLiveData
}
