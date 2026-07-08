package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cached short_effect text (English) for an ability, keyed by ability name —
// same disposable-cache treatment as PokemonEntity, not user data.
@Entity(tableName = "ability_description")
data class AbilityDescriptionEntity(
    @PrimaryKey val name: String,
    val description: String
)
