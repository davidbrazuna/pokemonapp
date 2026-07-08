package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidbrazuna.pokemonapp.R
import com.davidbrazuna.pokemonapp.ui.components.PokemonHomeTopBar

// Entry point / hub. Two square cards stacked and centered in the available
// space (both axes); if a third destination is added later this becomes the
// natural place for a LazyVerticalGrid instead — kept a plain Column while
// there are only two.
@Composable
fun HomeScreen(
    onPokedexClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PokemonHomeTopBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Center.Center on the vertical arrangement centers the whole
            // stack top-to-bottom; CenterHorizontally centers each card
            // left-to-right — together the stack sits in the middle of the
            // screen on both axes, not just pinned to the top.
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            HomeCard(
                icon = Icons.AutoMirrored.Filled.List,
                label = stringResource(R.string.home_card_pokedex_title),
                onClick = onPokedexClick
            )
            HomeCard(
                icon = Icons.Filled.Info,
                label = stringResource(R.string.home_card_about_title),
                onClick = onAboutClick
            )
        }
    }
}

// A single square entry card: icon centered above its label. Half the width
// of a full-width square, so two of them stacked stay compact instead of
// each spanning the whole screen. OutlinedCard (not Card) so there's no
// elevation shadow or tonal fill — just a flat surface with a border, per a
// squared-off, outlined look rather than a filled Material card.
@Composable
private fun HomeCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        shape = RectangleShape,
        colors = CardDefaults.outlinedCardColors(
            // A light gray tone, one step above the page background — same
            // tonal surface used by the top app bars, so it reads as part of
            // the same design system rather than an arbitrary gray.
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        // outlineVariant is Material3's subtle-border color: a light gray a
        // step darker than the background, not the stronger `outline` color.
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // decorative: the label text already conveys meaning
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(onPokedexClick = {}, onAboutClick = {})
}
