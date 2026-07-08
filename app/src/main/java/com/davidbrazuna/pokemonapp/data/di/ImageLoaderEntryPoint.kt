package com.davidbrazuna.pokemonapp.data.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

// PokemonApp builds Coil's ImageLoader in SingletonImageLoader.Factory, which is
// not a Hilt injection site. This entry point lets it pull the app-wide
// OkHttpClient out of the graph so Coil reuses the same client as Retrofit.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageLoaderEntryPoint {
    fun okHttpClient(): OkHttpClient
}
