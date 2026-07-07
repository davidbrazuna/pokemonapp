package com.davidbrazuna.pokemonapp.ui

// The PokeAPI returns names in lowercase ("ivysaur"); capitalize the first letter
// for display ("Ivysaur"). Used by both the list and the detail screen.
fun String.capitalizeForDisplay(): String =
    replaceFirstChar { it.uppercase() }
