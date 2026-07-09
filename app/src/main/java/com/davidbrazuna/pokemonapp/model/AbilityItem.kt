package com.davidbrazuna.pokemonapp.model

import kotlinx.serialization.Serializable

@Serializable
data class AbilityItem(
    val name: String,
    val url: String
)
