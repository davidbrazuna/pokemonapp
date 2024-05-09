package com.example.pokemonapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.pokemonapp.MainActivity.Companion.POKEMON_NAME
import com.example.pokemonapp.adapters.AbilitiesAdapter
import com.example.pokemonapp.databinding.PokemonDetailsBinding
import com.example.pokemonapp.viewmodel.PokemonDetailViewModel

class PokemonDetailsScreen : AppCompatActivity() {

    private lateinit var binding: PokemonDetailsBinding
    private lateinit var pokemonDetailsMvvm: PokemonDetailViewModel
    private lateinit var abilitiesAdapter: AbilitiesAdapter

    var name: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PokemonDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        abilitiesAdapter = AbilitiesAdapter()
        prepareAbilitiesRecyclerView()

        pokemonDetailsMvvm = ViewModelProvider(this)[PokemonDetailViewModel::class.java]

        getPokemonDetailsFromIntent()

        pokemonDetailsMvvm.getPokemonDetails(name!!)
        observePokemonDetailsLiveData()
    }

    private fun prepareAbilitiesRecyclerView() {
        binding.rvAbilities.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = abilitiesAdapter
        }
    }

    private fun observePokemonDetailsLiveData() {
        pokemonDetailsMvvm.observePokemonDetailsLiveData().observe(this, Observer {
            Glide.with(this)
                .load(it.sprites.other.home.front_default)
                .into(binding.imgPokemon)

            binding.pokemonName.text = "name: ${it.name}"
            binding.pokemonId.text = "id: ${it.id}"
            binding.pokemonHeight.text = "height: ${it.height.toString()}"
            binding.pokemonWeight.text = "weight: ${it.weight.toString()}"
            binding.pokemonBaseExperience.text = "base experience: ${it.base_experience.toString()}"
            abilitiesAdapter.setAbilitiesList(it.abilities)
        })
    }

    private fun getPokemonDetailsFromIntent() {
        val intent = intent
        name = intent.getStringExtra(POKEMON_NAME)

    }
}