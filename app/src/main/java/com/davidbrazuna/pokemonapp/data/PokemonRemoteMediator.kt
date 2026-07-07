package com.davidbrazuna.pokemonapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase
import com.davidbrazuna.pokemonapp.data.local.PokemonEntity
import com.davidbrazuna.pokemonapp.data.local.RemoteKeysEntity
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException

// Drives the network side of the offline-first list: Room is the single source
// of truth the UI reads, and this mediator fills/refreshes it from the PokeAPI.
// Keys are API offsets; the PokeAPI only paginates forward, so PREPEND ends
// immediately.
@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokemonApi,
    private val database: PokemonDatabase,
    private val pageSize: Int
) : RemoteMediator<Int, PokemonEntity>() {

    private val pokemonDao = database.pokemonDao()
    private val remoteKeysDao = database.remoteKeysDao()

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
                    val lastKeys = lastRemoteKeys(state)
                    // nextKey == null means the previous append reached the end.
                    // No last item at all means the list is empty and the initial
                    // REFRESH hasn't populated it yet — let it finish first.
                    val nextKey = lastKeys?.nextKey
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = lastKeys != null
                        )
                    nextKey
                }
            }

            val response = api.getPokemonList("pokemon?offset=$offset&limit=$pageSize")
            val endReached = response.next == null
            val entities = response.results.mapNotNull { it.toEntity() }

            val prevKey = if (offset == 0) null else (offset - pageSize).coerceAtLeast(0)
            val nextKey = if (endReached) null else offset + pageSize
            val keys = entities.map { RemoteKeysEntity(it.id, prevKey, nextKey) }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    pokemonDao.clearAll()
                    remoteKeysDao.clearRemoteKeys()
                }
                remoteKeysDao.insertAll(keys)
                pokemonDao.insertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // IOException/HttpException as well as serialization failures (e.g. a
            // captive portal returning HTML). Paging surfaces this as
            // LoadState.Error; the cached list stays intact.
            MediatorResult.Error(e)
        }
    }

    // Remote keys of the last cached item, used to find the next offset to append.
    private suspend fun lastRemoteKeys(
        state: PagingState<Int, PokemonEntity>
    ): RemoteKeysEntity? =
        state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { remoteKeysDao.remoteKeysByPokemonId(it.id) }
}
