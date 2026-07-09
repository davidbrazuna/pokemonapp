package com.davidbrazuna.pokemonapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbrazuna.pokemonapp.R

// Shared skeleton for the app's top bars. surfaceContainer gives the bar its
// tonal elevation (adapts to light/dark); no drop shadow, which M3 tonal surfaces
// make redundant. Only the navigation icon and actions differ per screen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonTopBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        title = { Text(title) },
        navigationIcon = navigationIcon,
        actions = { actions() }
    )
}

// Shared "Up" affordance: with a single-level hierarchy under Home, Up and Back
// always coincide, so every screen below Home uses the same arrow + callback.
@Composable
private fun BackNavigationIcon(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.action_back)
        )
    }
}

// Home screen: the app's entry point. No navigation icon — there is nothing
// "up" from here, so no back/home affordance is shown.
@Composable
fun PokemonHomeTopBar() {
    PokemonTopBar(title = stringResource(R.string.app_bar_home_title))
}

// List screen: back arrow to Home on the left, "Pokédex" centered, refresh
// action on the right.
@Composable
fun PokemonListTopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    PokemonTopBar(
        title = stringResource(R.string.app_bar_list_title),
        navigationIcon = { BackNavigationIcon(onBack) },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.action_refresh)
                )
            }
        }
    )
}

// About screen: back arrow to Home on the left, title centered.
@Composable
fun PokemonAboutTopBar(onBack: () -> Unit) {
    PokemonTopBar(
        title = stringResource(R.string.app_bar_about_title),
        navigationIcon = { BackNavigationIcon(onBack) }
    )
}

// Detail screen: back arrow (Up, to the List) on the left, Pokemon name
// centered, and a dedicated Home shortcut on the right. This screen sits three
// levels deep (Home -> List -> Detail), so Up alone would take two taps to
// reach Home; the extra action is a deliberate escape hatch, not a replacement
// for Up.
@Composable
fun PokemonDetailTopBar(title: String, onBack: () -> Unit, onHome: () -> Unit) {
    PokemonTopBar(
        title = title,
        navigationIcon = { BackNavigationIcon(onBack) },
        actions = {
            IconButton(onClick = onHome) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.action_home)
                )
            }
        }
    )
}
