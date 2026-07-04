package com.davidbrazuna.pokemonapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.davidbrazuna.pokemonapp.adapters.AbilitiesAdapter
import com.davidbrazuna.pokemonapp.databinding.PokemonDetailsBinding
import com.davidbrazuna.pokemonapp.viewmodel.PokemonDetailViewModel

class PokemonDetailsScreen : AppCompatActivity() {

    private lateinit var binding: PokemonDetailsBinding
    private lateinit var abilitiesAdapter: AbilitiesAdapter

    // The Intent extra (KEY_POKEMON_NAME) is read from SavedStateHandle inside the ViewModel.
    private val viewModel: PokemonDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PokemonDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        abilitiesAdapter = AbilitiesAdapter()
        prepareAbilitiesRecyclerView()

        observeDetails()
        observeError()
    }

    private fun prepareAbilitiesRecyclerView() {
        binding.rvAbilities.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = abilitiesAdapter
        }
    }

    private fun observeDetails() {
        viewModel.observePokemonDetailsLiveData().observe(this) { details ->
            Glide.with(this)
                .load(details.sprites.other?.home?.frontDefault)
                .into(binding.imgPokemon)

            binding.pokemonName.text = "name: ${details.name}"
            binding.pokemonId.text = "id: ${details.id}"
            binding.pokemonHeight.text = "height: ${details.height}"
            binding.pokemonWeight.text = "weight: ${details.weight}"
            binding.pokemonBaseExperience.text = "base experience: ${details.baseExperience}"
            abilitiesAdapter.setAbilitiesList(details.abilities)
        }
    }

    private fun observeError() {
        viewModel.observeErrorLiveData().observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
