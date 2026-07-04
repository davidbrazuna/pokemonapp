package com.davidbrazuna.pokemonapp.model

import kotlinx.serialization.Serializable

@Serializable
data class PokemonList(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<Pokemon> = emptyList()
)
