package com.davidbrazuna.pokemonapp.retrofit

import com.davidbrazuna.pokemonapp.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    // ignoreUnknownKeys lets the PokeAPI return extra fields we don't model
    private val json = Json { ignoreUnknownKeys = true }

    // Public so Coil's OkHttpNetworkFetcherFactory can reuse the same client
    // (shared connection pool and debug logging) instead of creating a second one.
    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        // Log request/response lines in debug builds only. BASIC (not BODY): this
        // client is shared with Coil for image fetches, and BODY would buffer and
        // try to print every sprite PNG in full, flooding logcat and drowning out
        // the JSON logs this exists for.
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }
        builder.build()
    }

    val api: PokemonApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PokemonApi::class.java)
    }
}
