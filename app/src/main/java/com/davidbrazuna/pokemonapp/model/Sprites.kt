package com.davidbrazuna.pokemonapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// All sprite URLs are nullable: the PokeAPI returns null for several Pokemon
// (alternate forms, newer entries). Declaring them non-null caused NPEs at runtime.
@Serializable
data class Sprites(
    @SerialName("back_default")
    val backDefault: String? = null,
    @SerialName("back_shiny")
    val backShiny: String? = null,
    @SerialName("front_default")
    val frontDefault: String? = null,
    @SerialName("front_shiny")
    val frontShiny: String? = null,
    val other: Other? = null
)

@Serializable
data class Other(
    val home: Home? = null
)

@Serializable
data class Home(
    @SerialName("front_default")
    val frontDefault: String? = null
)
