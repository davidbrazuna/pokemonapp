package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Instantiation lives in DatabaseModule (Hilt owns the single instance).
@Database(
    entities = [PokemonEntity::class, PagingMetadataEntity::class, AbilityDescriptionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun pagingMetadataDao(): PagingMetadataDao
    abstract fun abilityDescriptionDao(): AbilityDescriptionDao
}
