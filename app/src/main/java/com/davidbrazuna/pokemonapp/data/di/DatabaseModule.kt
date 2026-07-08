package com.davidbrazuna.pokemonapp.data.di

import android.content.Context
import androidx.room.Room
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Database graph, replacing PokemonDatabase.getInstance(). The @Singleton here is
// the single instance the whole app shares.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokemonDatabase =
        Room.databaseBuilder(context, PokemonDatabase::class.java, "pokemon.db")
            // This is a disposable network cache, not user data: on a schema
            // change just drop and rebuild it rather than shipping a migration —
            // the RemoteMediator refills it on next launch.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    // No per-DAO @Provides: the repository injects the whole PokemonDatabase and
    // the mediator reaches the DAOs off it (it needs database.withTransaction).
}
