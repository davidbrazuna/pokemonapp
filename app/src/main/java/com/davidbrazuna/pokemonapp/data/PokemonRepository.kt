package com.davidbrazuna.pokemonapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Single source for Pokemon data. The list is offline-first: Room is the source
// of truth the UI reads, kept in sync from the network by PokemonRemoteMediator.
// Detail calls are wrapped in Result so callers never deal with raw exceptions.
class PokemonRepository(
    private val api: PokemonApi,
    private val database: PokemonDatabase
) {

    // Room's DAO supplies the PagingSource; the RemoteMediator fetches pages into
    // Room. The Flow's element type stays PokemonWithImage, so the ViewModel and
    // UI are unchanged — the entity is mapped back to the existing UI model here.
    //
    // initialLoadSize == pageSize (PagingConfig defaults it to 3x): the mediator
    // fetches exactly one API page per load and stores the API's own next-offset,
    // so keeping the initial load one page wide keeps the mediator's offset in
    // step with what Paging actually loaded.
    @OptIn(ExperimentalPagingApi::class)
    fun getPokemonPager(): Flow<PagingData<PokemonWithImage>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE),
            remoteMediator = PokemonRemoteMediator(api, database, PAGE_SIZE),
            pagingSourceFactory = { database.pokemonDao().pagingSource() }
        ).flow.map { pagingData -> pagingData.map { it.toUiModel() } }

    suspend fun getPokemonDetails(name: String) = safeApiCall {
        api.getPokemonDetails(name)
    }

    // Runs [block] and wraps the outcome in Result. CancellationException is
    // rethrown rather than wrapped: leaving a screen mid-request cancels the
    // coroutine, and that cancellation must propagate normally instead of
    // being reported as a network error (would break structured concurrency).
    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
