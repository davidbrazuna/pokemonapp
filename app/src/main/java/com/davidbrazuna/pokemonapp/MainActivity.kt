package com.davidbrazuna.pokemonapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.davidbrazuna.pokemonapp.ui.PokemonDetailsScreen
import com.davidbrazuna.pokemonapp.ui.PokemonListScreen
import com.davidbrazuna.pokemonapp.viewmodel.PokemonDetailViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme = LocalConfiguration.current.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            // Match the View-based detail screen's Theme.Material3.DayNight, which
            // follows the system setting — without this MaterialTheme always
            // resolves light, flipping light list / dark detail on every navigation.
            val colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                Surface {
                    // targetSdk 36 forces edge-to-edge on Android 15+; Scaffold's
                    // innerPadding keeps the first row and the append footer clear
                    // of the status bar / navigation bar.
                    Scaffold { innerPadding ->
                        PokemonListScreen(
                            onPokemonClick = { pokemon ->
                                // The detail screen is still View-based; navigation stays
                                // via Intent this branch (moves to Navigation-Compose later).
                                val intent = Intent(this, PokemonDetailsScreen::class.java)
                                intent.putExtra(PokemonDetailViewModel.KEY_POKEMON_NAME, pokemon.name)
                                startActivity(intent)
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
