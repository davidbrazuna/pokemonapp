package com.example.pokemonapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemonapp.adapters.PokemonListAdapter
import com.example.pokemonapp.databinding.ActivityMainBinding
import com.example.pokemonapp.viewmodel.PokemonListViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pokemonListItemAdapter: PokemonListAdapter
    private lateinit var mainMvvm: PokemonListViewModel

    companion object {
        private const val VISIBLE_THRESHOLD = 2
        const val POKEMON_NAME = "POKEMON_NAME"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainMvvm = ViewModelProvider(this)[PokemonListViewModel::class.java]

        pokemonListItemAdapter = PokemonListAdapter()
        preparePokemonListRecyclerView()

        mainMvvm.getPokemonList()
        observePokemonListLiveData()

        onPokemonClick()
    }

    private fun onPokemonClick() {
        pokemonListItemAdapter.onItemClick = {
            val intent = Intent(this, PokemonDetailsScreen::class.java)
            intent.putExtra(POKEMON_NAME, it.name)
            startActivity(intent)
        }
    }

    private fun observePokemonListLiveData() {
        mainMvvm.observePokemonListLiveData().observe(this, Observer { pokemons ->
            pokemonListItemAdapter.setPokemonList(pokemons)
        })
    }

    private fun preparePokemonListRecyclerView(){
        binding.rvPokemons.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = pokemonListItemAdapter
            binding.rvPokemons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition <= VISIBLE_THRESHOLD) {
                        mainMvvm.getPreviousPage()
                    }
                    if (totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                        mainMvvm.getNextPage()
                    }
                }
            })
        }
    }
}