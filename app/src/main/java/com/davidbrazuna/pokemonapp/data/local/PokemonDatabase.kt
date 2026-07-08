package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Instantiation lives in DatabaseModule (Hilt owns the single instance).
@Database(
    entities = [PokemonEntity::class, PagingMetadataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun pagingMetadataDao(): PagingMetadataDao
}
