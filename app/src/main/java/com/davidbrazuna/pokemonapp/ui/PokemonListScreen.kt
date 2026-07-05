package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.viewmodel.PokemonListViewModel

@Composable
fun PokemonListScreen(
    onPokemonClick: (PokemonWithImage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = viewModel()
) {
    val items = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()

    when (val refresh = items.loadState.refresh) {
        is LoadState.Loading -> FullScreenLoading(modifier)
        is LoadState.Error -> FullScreenError(
            message = refresh.error.message,
            onRetry = items::retry,
            modifier = modifier
        )
        else -> PokemonList(items = items, onPokemonClick = onPokemonClick, modifier = modifier)
    }
}

@Composable
private fun PokemonList(
    items: LazyPagingItems<PokemonWithImage>,
    onPokemonClick: (PokemonWithImage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.name }
        ) { index ->
            items[index]?.let { pokemon ->
                PokemonRow(pokemon = pokemon, onClick = { onPokemonClick(pokemon) })
            }
        }

        // Footer reflecting the append (load-more) state.
        when (val append = items.loadState.append) {
            is LoadState.Loading -> item { AppendLoading() }
            is LoadState.Error -> item {
                AppendError(message = append.error.message, onRetry = items::retry)
            }
            else -> Unit
        }
    }
}

@Composable
private fun PokemonRow(
    pokemon: PokemonWithImage,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val placeholder = ColorPainter(Color.LightGray)
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            placeholder = placeholder,
            error = placeholder,
            fallback = placeholder,
            modifier = Modifier
                .size(64.dp)
                .padding(end = 16.dp)
        )
        Text(text = pokemon.name, fontSize = 20.sp)
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        RetryContent(message = message, onRetry = onRetry)
    }
}

@Composable
private fun AppendLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AppendError(message: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        RetryContent(message = message, onRetry = onRetry)
    }
}

@Composable
private fun RetryContent(message: String?, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message ?: "Something went wrong",
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}
