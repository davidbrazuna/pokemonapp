package com.davidbrazuna.pokemonapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidbrazuna.pokemonapp.ui.PokemonDetailScreen
import com.davidbrazuna.pokemonapp.ui.PokemonListScreen
import com.davidbrazuna.pokemonapp.ui.navigation.Route

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Surface {
                    Scaffold { innerPadding ->
                        PokemonApp(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.PokemonList,
        modifier = modifier
    ) {
        composable<Route.PokemonList> {
            PokemonListScreen(
                onPokemonClick = { pokemon ->
                    navController.navigate(Route.PokemonDetail(pokemon.name))
                }
            )
        }
        composable<Route.PokemonDetail> {
            // Route args land in the ViewModel's SavedStateHandle automatically.
            PokemonDetailScreen()
        }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val isDarkTheme = LocalConfiguration.current.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}
