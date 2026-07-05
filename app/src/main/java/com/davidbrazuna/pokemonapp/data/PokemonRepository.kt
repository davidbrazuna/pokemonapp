package com.davidbrazuna.pokemonapp.data

import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// Result holder for a page of Pokemon plus the paging URLs from the API response.
data class PokemonPage(
    val pokemons: List<PokemonWithImage>,
    val nextUrl: String?,
    val previousUrl: String?
)

// Single source for Pokemon data. Wraps network calls in Result so callers
// never deal with raw exceptions (no crash on IO/HTTP failures).
class PokemonRepository(
    private val api: PokemonApi
) {

    suspend fun getPokemonPage(url: String): Result<PokemonPage> = safeApiCall {
        coroutineScope {
            val listResponse = api.getPokemonList(url)
            // NOTE: this still fetches details per Pokemon just for the sprite (N+1).
            // Kept intentionally for now; to be removed in the pagination branch by
            // deriving the sprite URL from the id.
            val pokemons = listResponse.results
                .map { pokemon ->
                    async {
                        val imageUrl = safeApiCall {
                            api.getPokemonDetails(pokemon.name).sprites.frontDefault
                        }.getOrNull()
                        PokemonWithImage(pokemon.name, imageUrl)
                    }
                }
                .awaitAll()

            PokemonPage(
                pokemons = pokemons,
                nextUrl = listResponse.next,
                previousUrl = listResponse.previous
            )
        }
    }

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
}
