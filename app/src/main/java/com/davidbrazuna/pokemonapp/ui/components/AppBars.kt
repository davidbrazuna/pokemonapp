package com.davidbrazuna.pokemonapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

// List screen: "Pokédex" centered, refresh action on the right.
@Composable
fun PokemonListTopBar(onRefresh: () -> Unit) {
    PokemonTopBar(
        title = stringResource(R.string.app_bar_list_title),
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

// Detail screen: back arrow on the left, Pokemon name centered.
@Composable
fun PokemonDetailTopBar(title: String, onBack: () -> Unit) {
    PokemonTopBar(
        title = title,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                )
            }
        }
    )
}
