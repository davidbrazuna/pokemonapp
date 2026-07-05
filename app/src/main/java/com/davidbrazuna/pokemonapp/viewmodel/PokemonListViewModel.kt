package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.Flow

class PokemonListViewModel(
    private val repository: PokemonRepository = PokemonRepository(RetrofitInstance.api)
) : ViewModel() {

    // Paging handles loading/append/error/retry state; the UI collects this flow
    // with collectAsLazyPagingItems(). cachedIn keeps the paged data across
    // configuration changes and recompositions.
    val pokemonPagingFlow: Flow<PagingData<PokemonWithImage>> =
        repository.getPokemonPager().cachedIn(viewModelScope)
}
