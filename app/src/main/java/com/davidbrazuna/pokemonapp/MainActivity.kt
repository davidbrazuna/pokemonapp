package com.davidbrazuna.pokemonapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.davidbrazuna.pokemonapp.ui.PokemonDetailsScreen
import com.davidbrazuna.pokemonapp.ui.PokemonListScreen
import com.davidbrazuna.pokemonapp.viewmodel.PokemonDetailViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    PokemonListScreen(
                        onPokemonClick = { pokemon ->
                            // The detail screen is still View-based; navigation stays
                            // via Intent this branch (moves to Navigation-Compose later).
                            val intent = Intent(this, PokemonDetailsScreen::class.java)
                            intent.putExtra(PokemonDetailViewModel.KEY_POKEMON_NAME, pokemon.name)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
