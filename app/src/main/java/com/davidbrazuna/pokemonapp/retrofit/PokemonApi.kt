package com.davidbrazuna.pokemonapp.retrofit

import com.davidbrazuna.pokemonapp.model.AbilityDetailResponse
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.model.PokemonList
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface PokemonApi {

    @GET
    suspend fun getPokemonList(@Url url: String): PokemonList

    @GET("pokemon/{name}")
    suspend fun getPokemonDetails(@Path("name") name: String): PokemonDetailResponseData

    // AbilityItem.url is already an absolute PokeAPI url, same @Url pattern as
    // getPokemonList.
    @GET
    suspend fun getAbilityDetail(@Url url: String): AbilityDetailResponse
}
