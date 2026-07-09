package com.davidbrazuna.pokemonapp.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation-Compose routes. Each route is a @Serializable object/class;
// its fields become the destination arguments (and land in the SavedStateHandle).
sealed interface Route {

    // Entry point / hub: a permanent screen the user returns to, not a disposable
    // splash-like destination. Kept in the back stack under List/Detail/About.
    @Serializable
    data object Home : Route

    @Serializable
    data object PokemonList : Route

    @Serializable
    data class PokemonDetail(val name: String) : Route

    @Serializable
    data object About : Route
}
