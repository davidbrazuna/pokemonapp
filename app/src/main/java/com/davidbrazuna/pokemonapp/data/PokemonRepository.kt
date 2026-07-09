package com.davidbrazuna.pokemonapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.davidbrazuna.pokemonapp.data.local.AbilityDescriptionEntity
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase
import com.davidbrazuna.pokemonapp.model.AbilityItem
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Single source for Pokemon data. The list is offline-first: Room is the source
// of truth the UI reads, kept in sync from the network by PokemonRemoteMediator.
// Detail calls are wrapped in Result so callers never deal with raw exceptions.
//
// @Singleton: stateless, so one shared instance is fine (and lets both VMs share
// the same Pager/cache wiring).
@Singleton
class PokemonRepository @Inject constructor(
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
            remoteMediator = PokemonRemoteMediator(
                api = api,
                pokemonDao = database.pokemonDao(),
                metadataDao = database.pagingMetadataDao(),
                transactor = RoomTransactor(database),
                pageSize = PAGE_SIZE
            ),
            pagingSourceFactory = { database.pokemonDao().pagingSource() }
        ).flow.map { pagingData -> pagingData.map { it.toUiModel() } }

    suspend fun getPokemonDetails(name: String) = safeApiCall {
        api.getPokemonDetails(name)
    }

    // Cache-first: a local read failure (e.g. a corrupt DB) degrades to a
    // network fetch rather than failing outright — the cache is an optimization,
    // not something that should surface as "the request failed" on its own. Only
    // the network fetch is wrapped in safeApiCall, so a Result.failure here
    // always means the network attempt failed, never an ambiguous local error.
    suspend fun getAbilityDescription(ability: AbilityItem): Result<String> {
        val cached = readCachedAbilityDescription(ability.name)
        if (cached != null) return Result.success(cached)
        return safeApiCall { fetchAndCacheAbilityDescription(ability) }
    }

    private suspend fun readCachedAbilityDescription(name: String): String? =
        try {
            database.abilityDescriptionDao().get(name)?.description
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private suspend fun fetchAndCacheAbilityDescription(ability: AbilityItem): String {
        val response = api.getAbilityDetail(ability.url)
        val description = response.effectEntries
            .firstOrNull { it.language.name == "en" }
            ?.shortEffect
            ?: NO_DESCRIPTION_FALLBACK
        // Best-effort write: if it fails (disk full, corrupt DB), the worst
        // outcome is losing the cache for next time, not losing the response
        // we already have in hand. Left inside safeApiCall, an insert failure
        // here would turn an already-successful network fetch into a reported
        // Result.failure, contradicting getAbilityDescription's own guarantee
        // that a failure always means the network attempt failed.
        runCatching {
            database.abilityDescriptionDao().insert(
                AbilityDescriptionEntity(name = ability.name, description = description)
            )
        }
        return description
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
        private const val NO_DESCRIPTION_FALLBACK = "No description available."
    }
}
