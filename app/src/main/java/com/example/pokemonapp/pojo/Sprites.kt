package com.example.pokemonapp.pojo

data class Sprites(
    val back_default: String,
    val back_shiny: String,
    val front_default: String,
    val front_shiny: String,
    val other: Other
)

data class Other(val home: Home)

data class Home(val front_default: String)