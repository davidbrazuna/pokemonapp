package com.davidbrazuna.pokemonapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.davidbrazuna.pokemonapp.data.local.PagingMetadataDao
import com.davidbrazuna.pokemonapp.data.local.PagingMetadataEntity
import com.davidbrazuna.pokemonapp.data.local.PokemonDao
import com.davidbrazuna.pokemonapp.data.local.PokemonEntity
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

// Drives the network side of the offline-first list: Room is the single source
// of truth the UI reads, and this mediator fills/refreshes it from the PokeAPI.
// The API only paginates forward, so the only state carried between loads is a
// single "next offset" (PagingMetadataEntity) taken from the API's own `next`
// link — never inferred from the cached rows' ids, which don't map to offsets
// (special forms sit at ids 10001+).
@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokemonApi,
    private val pokemonDao: PokemonDao,
    private val metadataDao: PagingMetadataDao,
    // Seam over Room's withTransaction so load() is unit-testable (see Transactor).
    private val transactor: Transactor,
    private val pageSize: Int,
    // Cache lifetime for the initialize() TTL. Injectable so tests can pin it.
    private val cacheTimeout: Duration = 1.hours,
    // Wall clock, injectable for the same reason.
    private val now: () -> Long = System::currentTimeMillis
) : RemoteMediator<Int, PokemonEntity>() {

    // Skip the automatic launch REFRESH while the cache is still fresh. Without
    // this, every online cold start wipes the whole cache and re-inserts only
    // page 0 — truncating a list the user had scrolled deep into (and dropping
    // their scroll position). A stale (or absent) cache still refreshes.
    override suspend fun initialize(): InitializeAction {
        val lastUpdated = metadataDao.get()?.lastUpdated ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val age = now() - lastUpdated
        return if (age <= cacheTimeout.inWholeMilliseconds) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                // The PokeAPI has no backward pagination; nothing to prepend.
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // nextOffset == null means a previous load reached the last
                    // page. No metadata row at all means REFRESH hasn't run yet —
                    // treat as "not the end" so Paging lets the refresh happen.
                    val metadata = metadataDao.get()
                        ?: return MediatorResult.Success(endOfPaginationReached = false)
                    metadata.nextOffset
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            val response = api.getPokemonList("pokemon?offset=$offset&limit=$pageSize")
            // The API's `next` link is authoritative for the following offset;
            // parse it rather than computing offset + pageSize (which assumes the
            // server honored `limit` exactly). A null `next` means end reached.
            val nextOffset = offsetFromUrl(response.next)
            val entities = response.results.mapNotNull { it.toEntity() }

            transactor.run {
                if (loadType == LoadType.REFRESH) {
                    pokemonDao.clearAll()
                }
                pokemonDao.insertAll(entities)
                metadataDao.upsert(
                    PagingMetadataEntity(nextOffset = nextOffset, lastUpdated = now())
                )
            }

            MediatorResult.Success(endOfPaginationReached = nextOffset == null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // IOException/HttpException as well as serialization failures (e.g. a
            // captive portal returning HTML). Paging surfaces this as
            // LoadState.Error; the cached list stays intact.
            MediatorResult.Error(e)
        }
    }

    companion object {
        // PokeAPI `next` urls look like ...?offset=40&limit=20; pull the offset.
        // Null (no next page) or an unparseable url both yield null = end reached.
        private fun offsetFromUrl(url: String?): Int? =
            url?.substringAfter("offset=", "")
                ?.substringBefore('&')
                ?.toIntOrNull()
    }
}
