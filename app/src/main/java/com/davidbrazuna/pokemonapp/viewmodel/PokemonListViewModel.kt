package com.davidbrazuna.pokemonapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.davidbrazuna.pokemonapp.data.PokemonRepository
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.Flow

// AndroidViewModel so the repository can be given the Room database, which needs
// a Context. Built directly (not constructor-injected) because the default
// factory only auto-injects Application/SavedStateHandle. Revisit once Hilt lands.
class PokemonListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = PokemonRepository(
        RetrofitInstance.api,
        PokemonDatabase.getInstance(application)
    )

    // Paging handles loading/append/error/retry state; the UI collects this flow
    // with collectAsLazyPagingItems(). cachedIn keeps the paged data across
    // configuration changes and recompositions.
    val pokemonPagingFlow: Flow<PagingData<PokemonWithImage>> =
        repository.getPokemonPager().cachedIn(viewModelScope)
}
