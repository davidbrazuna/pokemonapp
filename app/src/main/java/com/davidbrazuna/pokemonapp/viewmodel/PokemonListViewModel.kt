package com.davidbrazuna.pokemonapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Hilt injects the repository, so this is a plain ViewModel — no AndroidViewModel
// or Context plumbing needed (the database Context is resolved in DatabaseModule).
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    // Paging handles loading/append/error/retry state; the UI collects this flow
    // with collectAsLazyPagingItems(). cachedIn keeps the paged data across
    // configuration changes and recompositions.
    val pokemonPagingFlow: Flow<PagingData<PokemonWithImage>> =
        repository.getPokemonPager().cachedIn(viewModelScope)
}
