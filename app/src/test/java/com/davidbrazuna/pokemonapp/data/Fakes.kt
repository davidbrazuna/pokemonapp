package com.davidbrazuna.pokemonapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.davidbrazuna.pokemonapp.data.local.PagingMetadataDao
import com.davidbrazuna.pokemonapp.data.local.PagingMetadataEntity
import com.davidbrazuna.pokemonapp.data.local.PokemonDao
import com.davidbrazuna.pokemonapp.data.local.PokemonEntity
import com.davidbrazuna.pokemonapp.model.Pokemon
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.model.PokemonList
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi

// Scripted PokemonApi: each getPokemonList call pops the next queued response (or
// throws the queued error). Records the urls it was called with so tests can
// assert the requested offset.
class FakePokemonApi : PokemonApi {
    // Each element is either a PokemonList to return or a Throwable to throw.
    private val responses = ArrayDeque<Result<PokemonList>>()
    val requestedUrls = mutableListOf<String>()

    fun enqueue(list: PokemonList) = responses.addLast(Result.success(list))
    fun enqueueError(error: Throwable) = responses.addLast(Result.failure(error))

    override suspend fun getPokemonList(url: String): PokemonList {
        requestedUrls += url
        val next = responses.removeFirstOrNull()
            ?: error("FakePokemonApi: no response queued for $url")
        return next.getOrThrow()
    }

    override suspend fun getPokemonDetails(name: String): PokemonDetailResponseData =
        throw NotImplementedError("not used by mediator tests")
}

// In-memory PokemonDao. insertAll upserts by id (mirrors REPLACE); pagingSource
// is unused by load() so it's not implemented.
class FakePokemonDao : PokemonDao {
    val items = mutableListOf<PokemonEntity>()
    var clearAllCount = 0
        private set

    override fun pagingSource(): PagingSource<Int, PokemonEntity> =
        throw NotImplementedError("pagingSource is covered by instrumented tests")

    override suspend fun insertAll(pokemon: List<PokemonEntity>) {
        pokemon.forEach { entity ->
            items.removeAll { it.id == entity.id }
            items += entity
        }
    }

    override suspend fun clearAll() {
        clearAllCount++
        items.clear()
    }
}

// In-memory single-row metadata store.
class FakePagingMetadataDao : PagingMetadataDao {
    private var stored: PagingMetadataEntity? = null

    override suspend fun upsert(metadata: PagingMetadataEntity) {
        stored = metadata
    }

    override suspend fun get(id: Int): PagingMetadataEntity? = stored

    override suspend fun clear() {
        stored = null
    }
}

// Runs the block without a real transaction (the seam under test).
class RunningTransactor : Transactor {
    override suspend fun <R> run(block: suspend () -> R): R = block()
}

// Helpers to build API list responses keyed by id (the mediator derives the id,
// and the sprite url, from each item's url — see PokemonMapping.toEntity()).
fun pokemonList(ids: IntRange, next: String?): PokemonList =
    PokemonList(
        count = 2000,
        next = next,
        previous = null,
        results = ids.map { id ->
            Pokemon(name = "pokemon-$id", url = "https://pokeapi.co/api/v2/pokemon/$id/")
        }
    )

// An empty PagingState — load() ignores its state argument (the append offset
// comes from the metadata row, not the state), so one shared empty state is fine.
fun <T : Any> emptyPagingState(): PagingState<Int, T> =
    PagingState(pages = emptyList(), anchorPosition = null, config = pagingConfig(), leadingPlaceholderCount = 0)

private fun pagingConfig() = androidx.paging.PagingConfig(pageSize = 20)
