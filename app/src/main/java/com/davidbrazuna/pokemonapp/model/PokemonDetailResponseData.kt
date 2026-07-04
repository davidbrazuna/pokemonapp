package com.davidbrazuna.pokemonapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponseData(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    @SerialName("base_experience")
    val baseExperience: Int,
    val abilities: List<Ability> = emptyList(),
    val sprites: Sprites
)
