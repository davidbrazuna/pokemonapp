package com.davidbrazuna.pokemonapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import coil3.compose.AsyncImage

// A single grey painter reused for placeholder/loading/error so it isn't
// reallocated on every recomposition of a row.
private val SpritePlaceholder = ColorPainter(Color.LightGray)

// Shared sprite image: same Coil + grey-placeholder recipe used by both the
// list rows and the detail screen.
@Composable
fun PokemonSprite(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        placeholder = SpritePlaceholder,
        error = SpritePlaceholder,
        modifier = modifier
    )
}
