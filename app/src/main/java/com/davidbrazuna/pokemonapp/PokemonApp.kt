package com.davidbrazuna.pokemonapp

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.davidbrazuna.pokemonapp.data.di.ImageLoaderEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import okio.Path.Companion.toOkioPath

// Coil 3 ships no networking by default. Implementing SingletonImageLoader.Factory
// wires the app-wide ImageLoader once so every AsyncImage can fetch over HTTP,
// reusing the app-wide OkHttpClient from the Hilt graph (same connection pool,
// same debug logging as Retrofit). @HiltAndroidApp generates the root component.
@HiltAndroidApp
class PokemonApp : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // newImageLoader is not a Hilt injection site, so reach the shared client
        // through an entry point instead of @Inject.
        val okHttpClient = EntryPointAccessors
            .fromApplication(this, ImageLoaderEntryPoint::class.java)
            .okHttpClient()
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { okHttpClient }
                    )
                )
            }
            // A custom ImageLoader keeps the default memory cache but NOT the disk
            // cache, so without this every sprite is re-fetched from the network on
            // each scroll-back / reopen / restart. Enabling both caches makes seen
            // sprites appear instantly; crossfade smooths their first appearance.
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
