package com.davidbrazuna.pokemonapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ability(
    val ability: AbilityItem,
    @SerialName("is_hidden")
    val isHidden: Boolean,
    val slot: Int
)
