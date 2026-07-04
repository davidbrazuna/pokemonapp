package com.davidbrazuna.pokemonapp.retrofit

import com.davidbrazuna.pokemonapp.pojo.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.pojo.PokemonList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface PokemonApi {

     @GET
     fun getPokemonList(@Url url: String): Call<PokemonList>

     @GET("pokemon/{name}")
     fun getPokemonDetails(@Path("name") name: String): Call<PokemonDetailResponseData>

}