package com.example.pokemonapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemonapp.retrofit.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class PokemonListViewModel : ViewModel() {

    private val pokemonListLiveData = MutableLiveData<List<PokemonWithImage>>()
    private var nextPageUrl: String? = null
    private var previousPageUrl: String? = null

    companion object {
        private const val INITIAL_URL = "https://pokeapi.co/api/v2/pokemon/?offset=0&limit=20"
    }

    init {
        getPokemonList(INITIAL_URL)
    }

    fun getPokemonList(url: String = INITIAL_URL) {
        viewModelScope.launch {
            val response = RetrofitInstance.api.getPokemonList(url).awaitResponse()
            if (response.isSuccessful) {
                val pokemonListResponse = response.body()
                nextPageUrl = pokemonListResponse?.next
                previousPageUrl = pokemonListResponse?.previous
                val pokemonDetails = pokemonListResponse?.results?.map { pokemon ->
                    async {
                        val detailResponse = RetrofitInstance.api.getPokemonDetails(pokemon.name).awaitResponse()
                        if (detailResponse.isSuccessful) {
                            val imageUrl = detailResponse.body()?.sprites?.front_default ?: ""
                            PokemonWithImage(pokemon.name, imageUrl)
                        } else null
                    }
                }?.awaitAll()?.filterNotNull()
                pokemonListLiveData.postValue(pokemonDetails!!)
            }
        }
    }

    fun getNextPage() {
        nextPageUrl?.let {
            getPokemonList(it)
        }
    }

    fun getPreviousPage() {
        previousPageUrl?.let {
            getPokemonList(it)
        }
    }

    fun observePokemonListLiveData(): LiveData<List<PokemonWithImage>> {
        return pokemonListLiveData
    }
}

data class PokemonWithImage(val name: String, val imageUrl: String)

