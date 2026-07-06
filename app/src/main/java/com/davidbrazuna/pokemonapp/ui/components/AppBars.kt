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
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbrazuna.pokemonapp.R

// Light-grey container (surfaceContainer adapts to light/dark) plus a drop shadow,
// so the bar reads as a raised surface above the scrolling content.
private val AppBarElevation = 4.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appBarColors(): TopAppBarColors =
    TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

private fun Modifier.appBarShadow(): Modifier = shadow(elevation = AppBarElevation)

// Top bar for the list screen: "Pokédex" centered, refresh action on the right.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListTopBar(onRefresh: () -> Unit) {
    CenterAlignedTopAppBar(
        modifier = Modifier.appBarShadow(),
        colors = appBarColors(),
        title = { Text(stringResource(R.string.app_bar_list_title)) },
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

// Top bar for the detail screen: back arrow on the left, Pokemon name centered.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailTopBar(title: String, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        modifier = Modifier.appBarShadow(),
        colors = appBarColors(),
        title = { Text(title) },
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
