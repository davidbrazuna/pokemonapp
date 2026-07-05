package com.davidbrazuna.pokemonapp.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

// Single source for Pokemon data. The list is served through Paging 3; detail
// calls are wrapped in Result so callers never deal with raw exceptions.
class PokemonRepository(
    private val api: PokemonApi
) {

    // Paging 3 owns loading/append/retry state; the sprite is derived from the id
    // inside PokemonPagingSource, so no per-Pokemon detail call is made.
    //
    // initialLoadSize must match pageSize: PagingConfig defaults it to 3x pageSize,
    // but PokemonPagingSource derives prevKey/nextKey from params.loadSize, so a
    // mismatched initial load leaves a gap/overlap between the refresh page and
    // subsequent prepend/append pages (and duplicate keys in the list).
    fun getPokemonPager(): Flow<PagingData<PokemonWithImage>> =
        Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE)) {
            PokemonPagingSource(api)
        }.flow

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
