package com.davidbrazuna.pokemonapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidbrazuna.pokemonapp.adapters.PokemonListAdapter
import com.davidbrazuna.pokemonapp.databinding.ActivityMainBinding
import com.davidbrazuna.pokemonapp.viewmodel.PokemonDetailViewModel
import com.davidbrazuna.pokemonapp.viewmodel.PokemonListViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pokemonListItemAdapter: PokemonListAdapter
    private val viewModel: PokemonListViewModel by viewModels()

    companion object {
        private const val VISIBLE_THRESHOLD = 2
        const val POKEMON_NAME = "POKEMON_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pokemonListItemAdapter = PokemonListAdapter()
        preparePokemonListRecyclerView()

        // The list load is triggered by the ViewModel's init, so it survives
        // configuration changes and is not re-triggered on every onCreate.
        observePokemonList()
        observeError()

        onPokemonClick()
    }

    private fun onPokemonClick() {
        pokemonListItemAdapter.onItemClick = {
            val intent = Intent(this, PokemonDetailsScreen::class.java)
            intent.putExtra(PokemonDetailViewModel.KEY_POKEMON_NAME, it.name)
            startActivity(intent)
        }
    }

    private fun observePokemonList() {
        viewModel.observePokemonListLiveData().observe(this) { pokemons ->
            pokemonListItemAdapter.setPokemonList(pokemons)
        }
    }

    private fun observeError() {
        viewModel.observeErrorLiveData().observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun preparePokemonListRecyclerView() {
        binding.rvPokemons.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = pokemonListItemAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition <= VISIBLE_THRESHOLD) {
                        viewModel.getPreviousPage()
                    }
                    if (totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                        viewModel.getNextPage()
                    }
                }
            })
        }
    }
}
