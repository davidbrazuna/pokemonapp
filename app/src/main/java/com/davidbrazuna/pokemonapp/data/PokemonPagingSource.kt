package com.davidbrazuna.pokemonapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

// Pages the PokeAPI list endpoint. The key is the offset; loadSize is the limit.
// The sprite URL is derived from the id already present in each list item's url,
// so no per-Pokemon detail call is made (this is the N+1 fix).
class PokemonPagingSource(
    private val api: PokemonApi
) : PagingSource<Int, PokemonWithImage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonWithImage> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val response = api.getPokemonList("pokemon?offset=$offset&limit=$limit")
            val pokemons = response.results.map { pokemon ->
                PokemonWithImage(pokemon.name, spriteUrlFor(pokemon.url))
            }
            LoadResult.Page(
                data = pokemons,
                prevKey = if (offset == 0) null else (offset - limit).coerceAtLeast(0),
                // A null `next` from the API means there is no further page.
                nextKey = if (response.next == null) null else offset + limit
            )
        } catch (e: CancellationException) {
            // Leaving the screen cancels the load; let cancellation propagate
            // normally instead of surfacing it as a load error.
            throw e
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PokemonWithImage>): Int? {
        // Refresh from roughly the page closest to the current anchor position.
        val anchor = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchor) ?: return null
        return closestPage.prevKey?.plus(state.config.pageSize)
            ?: closestPage.nextKey?.minus(state.config.pageSize)
    }

    companion object {
        // List item urls look like https://pokeapi.co/api/v2/pokemon/{id}/ — the id
        // is the last non-empty path segment. The official sprite repo is keyed by id.
        private fun spriteUrlFor(url: String): String? {
            val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
        }
    }
}
