package com.davidbrazuna.pokemonapp.model

// UI model for the list: a Pokemon name paired with its sprite URL.
data class PokemonWithImage(
    val name: String,
    val imageUrl: String?
)
