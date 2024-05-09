package com.example.pokemonapp.pojo

data class PokemonDetailResponseData(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val base_experience: Int,
    val abilities: List<Ability>,
    val sprites: Sprites,
)