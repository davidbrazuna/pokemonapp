package com.davidbrazuna.pokemonapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidbrazuna.pokemonapp.ui.PokemonDetailScreen
import com.davidbrazuna.pokemonapp.ui.PokemonListScreen
import com.davidbrazuna.pokemonapp.ui.navigation.Route
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint enables Hilt injection for the ViewModels created by the
// NavHost below (via hiltViewModel()).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                // Each screen owns its own Scaffold + TopAppBar, so there is no
                // app-level Scaffold here; the screens' Scaffolds consume the insets.
                Surface {
                    PokemonNavHost()
                }
            }
        }
    }
}

@Composable
private fun PokemonNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.PokemonList
    ) {
        composable<Route.PokemonList> {
            PokemonListScreen(
                onPokemonClick = { pokemon ->
                    navController.navigate(Route.PokemonDetail(pokemon.name))
                }
            )
        }
        composable<Route.PokemonDetail> {
            // Route args land in the ViewModel's SavedStateHandle automatically;
            // the screen derives its own title from there.
            PokemonDetailScreen(onBack = navController::navigateUp)
        }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}
