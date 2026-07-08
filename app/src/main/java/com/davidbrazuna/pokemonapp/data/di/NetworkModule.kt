package com.davidbrazuna.pokemonapp.data.di

import com.davidbrazuna.pokemonapp.BuildConfig
import com.davidbrazuna.pokemonapp.retrofit.PokemonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

// Network graph, formerly the RetrofitInstance object. The OkHttpClient is a
// @Singleton so both Retrofit and Coil (via ImageLoaderEntryPoint) share one
// connection pool and one debug logger.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
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
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // ignoreUnknownKeys lets the PokeAPI return extra fields we don't model.
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun providePokemonApi(retrofit: Retrofit): PokemonApi =
        retrofit.create(PokemonApi::class.java)
}
