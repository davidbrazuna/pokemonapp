package com.davidbrazuna.pokemonapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, RemoteKeysEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var instance: PokemonDatabase? = null

        // Single app-wide instance; built lazily on first access. Uses the
        // application context to avoid leaking an Activity.
        fun getInstance(context: Context): PokemonDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PokemonDatabase::class.java,
                    "pokemon.db"
                ).build().also { instance = it }
            }
    }
}
