package com.davidbrazuna.pokemonapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidbrazuna.pokemonapp.ui.AboutScreen
import com.davidbrazuna.pokemonapp.ui.HomeScreen
import com.davidbrazuna.pokemonapp.ui.PokemonDetailScreen
import com.davidbrazuna.pokemonapp.ui.PokemonListScreen
import com.davidbrazuna.pokemonapp.ui.navigation.Route
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint enables Hilt injection for the ViewModels created by the
// NavHost below (via hiltViewModel()).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate() — the platform/compat library reads
        // the activity's theme (Theme.App.Starting) at that point to decide
        // what to show. postSplashScreenTheme in that style switches the
        // activity back to Theme.PokemonApp automatically once this is done.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Force dark (black) status-bar icons regardless of the system light/dark
        // setting: the app always renders on light surfaces (white background,
        // light-gray surfaceContainer top bar), so light icons would wash out.
        // Bare enableEdgeToEdge() uses systemDefault(), whose auto-threshold was
        // picking light icons here; light() pins them dark. The transparent-scrim
        // args keep the bars edge-to-edge (no solid color band).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Simple pop: the icon scales up slightly then shrinks to nothing
            // as it fades, instead of the default abrupt disappearance.
            // AnticipateInterpolator gives it a small "wind-up" before that
            // shrink, which reads as more deliberate than a linear scale-down.
            ObjectAnimator.ofPropertyValuesHolder(
                splashScreenView.iconView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.15f, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.15f, 0f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 1f, 0f)
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = EXIT_ANIMATION_DURATION_MS
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        splashScreenView.remove()
                    }
                })
                start()
            }
        }

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

    private companion object {
        const val EXIT_ANIMATION_DURATION_MS = 400L
    }
}

@Composable
private fun PokemonNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        // Home is a permanent hub, not a disposable splash: it stays in the
        // back stack under List/About, so the user can navigate back to it
        // normally instead of it being popped off after the first choice.
        startDestination = Route.Home
    ) {
        composable<Route.Home> {
            HomeScreen(
                onPokedexClick = {
                    navController.navigate(Route.PokemonList) { launchSingleTop = true }
                },
                onAboutClick = {
                    navController.navigate(Route.About) { launchSingleTop = true }
                }
            )
        }
        composable<Route.PokemonList> {
            // Only one level under Home, so Up and Back coincide: navigateUp
            // always lands back on Home here.
            PokemonListScreen(
                onPokemonClick = { pokemon ->
                    navController.navigate(Route.PokemonDetail(pokemon.name)) {
                        launchSingleTop = true
                    }
                },
                onBack = navController::navigateUp
            )
        }
        composable<Route.PokemonDetail> {
            // Route args land in the ViewModel's SavedStateHandle automatically;
            // the screen derives its own title from there.
            PokemonDetailScreen(
                onBack = navController::navigateUp,
                // Three levels deep (Home -> List -> Detail): Up alone would
                // take two taps to reach Home, so this is a deliberate escape
                // hatch that pops both List and Detail in one go, reusing the
                // existing Home instance instead of pushing a new one.
                onHome = {
                    navController.navigate(Route.Home) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Route.About> {
            AboutScreen(onBack = navController::navigateUp)
        }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}
