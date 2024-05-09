package com.example.pokemonapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pokemonapp.pojo.PokemonDetailResponseData
import com.example.pokemonapp.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PokemonDetailViewModel : ViewModel() {

    private val pokemonDetailsLiveData = MutableLiveData<PokemonDetailResponseData>()

    fun getPokemonDetails(name: String){

        RetrofitInstance.api.getPokemonDetails(name).enqueue(object : Callback<PokemonDetailResponseData> {
            override fun onResponse(
                call: Call<PokemonDetailResponseData>,
                response: Response<PokemonDetailResponseData>
            ) {
                response.body()?.let {
                    pokemonDetailsLiveData.postValue(it)
                }
            }

            override fun onFailure(call: Call<PokemonDetailResponseData>, t: Throwable) {
                Log.d("PokemonDetailsViewModel", t.message.toString())
            }

        })



    }
    fun observePokemonDetailsLiveData() : LiveData<PokemonDetailResponseData> {
        return pokemonDetailsLiveData
    }

}