package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.ui.components.LoadingIndicator
import com.davidbrazuna.pokemonapp.ui.components.PokemonListTopBar
import com.davidbrazuna.pokemonapp.ui.components.PokemonSprite
import com.davidbrazuna.pokemonapp.ui.components.RetryContent
import com.davidbrazuna.pokemonapp.viewmodel.PokemonListViewModel

@Composable
fun PokemonListScreen(
    onPokemonClick: (PokemonWithImage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = viewModel()
) {
    val items = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = { PokemonListTopBar(onRefresh = items::refresh) }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (val refresh = items.loadState.refresh) {
            is LoadState.Loading -> LoadingIndicator(contentModifier.fillMaxSize())
            is LoadState.Error -> RetryContent(
                message = refresh.error.message,
                onRetry = items::retry,
                modifier = contentModifier.fillMaxSize()
            )
            else -> PokemonList(
                items = items,
                onPokemonClick = onPokemonClick,
                modifier = contentModifier
            )
        }
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
                HorizontalDivider()
            }
        }

        // Footer reflecting the append (load-more) state.
        when (val append = items.loadState.append) {
            is LoadState.Loading -> item {
                LoadingIndicator(Modifier.fillMaxWidth().padding(16.dp))
            }
            is LoadState.Error -> item {
                RetryContent(
                    message = append.error.message,
                    onRetry = items::retry,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
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
        PokemonSprite(
            imageUrl = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier
                .size(64.dp)
                .padding(end = 16.dp)
        )
        Text(
            text = pokemon.name.capitalizeForDisplay(),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
