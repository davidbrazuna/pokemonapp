package com.davidbrazuna.pokemonapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import com.davidbrazuna.pokemonapp.data.local.PagingMetadataEntity
import com.davidbrazuna.pokemonapp.data.local.PokemonEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediatorTest {

    private val api = FakePokemonApi()
    private val pokemonDao = FakePokemonDao()
    private val metadataDao = FakePagingMetadataDao()

    private val fixedNow = 1_000_000L

    // Builds the mediator with a pinned clock; cacheTimeout defaults to 1h.
    private fun mediator(
        cacheTimeout: Duration = 1.hours,
        now: Long = fixedNow
    ) = PokemonRemoteMediator(
        api = api,
        pokemonDao = pokemonDao,
        metadataDao = metadataDao,
        transactor = RunningTransactor(),
        pageSize = 20,
        cacheTimeout = cacheTimeout,
        now = { now }
    )

    // --- nextOffset / offsetFromUrl -----------------------------------------

    @Test
    fun `refresh stores next offset parsed from the api next link`() = runTest {
        api.enqueue(pokemonList(1..20, next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"))

        val result = mediator().load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(20, metadataDao.get()?.nextOffset)
    }

    @Test
    fun `null next link means end of pagination and null next offset`() = runTest {
        api.enqueue(pokemonList(1..20, next = null))

        val result = mediator().load(LoadType.REFRESH, emptyPagingState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(null, metadataDao.get()?.nextOffset)
    }

    @Test
    fun `malformed next link yields null next offset (treated as end)`() = runTest {
        api.enqueue(pokemonList(1..20, next = "https://pokeapi.co/api/v2/pokemon?limit=20"))

        val result = mediator().load(LoadType.REFRESH, emptyPagingState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(null, metadataDao.get()?.nextOffset)
    }

    // --- initialize() TTL ----------------------------------------------------

    @Test
    fun `initialize launches refresh when there is no cached metadata`() = runTest {
        val action = mediator().initialize()
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }

    @Test
    fun `initialize skips refresh while cache is fresh`() = runTest {
        // lastUpdated 30 min ago, cacheTimeout 1h -> fresh.
        metadataDao.upsert(PagingMetadataEntity(nextOffset = 20, lastUpdated = fixedNow - 30 * 60 * 1000))

        val action = mediator().initialize()

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
    }

    @Test
    fun `initialize skips refresh exactly at the cache timeout boundary`() = runTest {
        // age == cacheTimeout must still be fresh: the check is `age <= timeout`.
        // Guards against a regression to `<`, which would refresh (and wipe) a
        // cache that is exactly at the boundary.
        metadataDao.upsert(PagingMetadataEntity(nextOffset = 20, lastUpdated = fixedNow - 1.hours.inWholeMilliseconds))

        val action = mediator(cacheTimeout = 1.hours).initialize()

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
    }

    @Test
    fun `initialize launches refresh once cache is stale`() = runTest {
        // lastUpdated 2h ago, cacheTimeout 1h -> stale.
        metadataDao.upsert(PagingMetadataEntity(nextOffset = 20, lastUpdated = fixedNow - 2 * 60 * 60 * 1000))

        val action = mediator().initialize()

        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }

    // --- LoadType branches ---------------------------------------------------

    @Test
    fun `prepend ends immediately without hitting the api`() = runTest {
        val result = mediator().load(LoadType.PREPEND, emptyPagingState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(api.requestedUrls.isEmpty())
    }

    @Test
    fun `append with no metadata does not hit the api and is not the end`() = runTest {
        val result = mediator().load(LoadType.APPEND, emptyPagingState())

        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(api.requestedUrls.isEmpty())
    }

    @Test
    fun `append with null next offset ends without hitting the api`() = runTest {
        metadataDao.upsert(PagingMetadataEntity(nextOffset = null, lastUpdated = fixedNow))

        val result = mediator().load(LoadType.APPEND, emptyPagingState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(api.requestedUrls.isEmpty())
    }

    @Test
    fun `append requests the stored next offset`() = runTest {
        metadataDao.upsert(PagingMetadataEntity(nextOffset = 40, lastUpdated = fixedNow))
        api.enqueue(pokemonList(41..60, next = "https://pokeapi.co/api/v2/pokemon?offset=60&limit=20"))

        mediator().load(LoadType.APPEND, emptyPagingState())

        assertEquals("pokemon?offset=40&limit=20", api.requestedUrls.single())
        assertEquals(60, metadataDao.get()?.nextOffset)
    }

    // --- REFRESH side effects & error path ----------------------------------

    @Test
    fun `refresh requests offset zero, clears the cache and inserts the page`() = runTest {
        // Seed a stale row to prove REFRESH clears it.
        pokemonDao.insertAll(listOf(seedEntity(999)))
        api.enqueue(pokemonList(1..20, next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"))

        mediator().load(LoadType.REFRESH, emptyPagingState())

        assertEquals("pokemon?offset=0&limit=20", api.requestedUrls.single())
        assertEquals(1, pokemonDao.clearAllCount)
        assertEquals(20, pokemonDao.items.size)
        assertTrue(pokemonDao.items.none { it.id == 999 })
        assertEquals(fixedNow, metadataDao.get()?.lastUpdated)
    }

    @Test
    fun `refresh with an all-unparseable page inserts nothing but stores next offset`() = runTest {
        // A whole page whose item urls carry no numeric id -> mapNotNull drops all
        // of them. The mediator must still record the next offset so APPEND can
        // move past this page instead of stalling on it (the PR#7 risk).
        api.enqueue(unparseableList(count = 20, next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"))

        val result = mediator().load(LoadType.REFRESH, emptyPagingState())

        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(pokemonDao.items.isEmpty())
        assertEquals(20, metadataDao.get()?.nextOffset)
    }

    @Test
    fun `append inserts the page without clearing the existing cache`() = runTest {
        // Existing page 1 in cache, metadata pointing at offset 20.
        pokemonDao.insertAll((1..20).map { seedEntity(it) })
        metadataDao.upsert(PagingMetadataEntity(nextOffset = 20, lastUpdated = fixedNow))
        api.enqueue(pokemonList(21..40, next = "https://pokeapi.co/api/v2/pokemon?offset=40&limit=20"))

        mediator().load(LoadType.APPEND, emptyPagingState())

        // APPEND must not clear — the whole point of paging into the cache.
        assertEquals(0, pokemonDao.clearAllCount)
        assertEquals(40, pokemonDao.items.size)
        assertEquals(40, metadataDao.get()?.nextOffset)
    }

    @Test
    fun `api failure surfaces as error and leaves the cache untouched`() = runTest {
        pokemonDao.insertAll(listOf(seedEntity(1)))
        api.enqueueError(IOException("no network"))

        val result = mediator().load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        // NOTE: this asserts ordering (the API call precedes the transaction, so a
        // failure clears nothing) — NOT transactional atomicity. RunningTransactor
        // executes the block plainly; real REFRESH rollback is an instrumented test.
        assertEquals(0, pokemonDao.clearAllCount)
        assertEquals(1, pokemonDao.items.size)
    }

    private fun seedEntity(id: Int) =
        PokemonEntity(id = id, name = "seed-$id", imageUrl = null)
}
