package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.davidbrazuna.pokemonapp.R
import com.davidbrazuna.pokemonapp.model.PokemonWithImage
import com.davidbrazuna.pokemonapp.ui.components.LoadingIndicator
import com.davidbrazuna.pokemonapp.ui.components.PokemonListTopBar
import com.davidbrazuna.pokemonapp.ui.components.PokemonSprite
import com.davidbrazuna.pokemonapp.ui.components.RetryContent
import com.davidbrazuna.pokemonapp.viewmodel.PokemonListViewModel
import kotlinx.coroutines.flow.flowOf

// Stateful entry point: binds the ViewModel and delegates to the stateless body,
// mirroring the split used by PokemonDetailScreen.
@Composable
fun PokemonListScreen(
    onPokemonClick: (PokemonWithImage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val items = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()
    PokemonListScreen(
        items = items,
        onPokemonClick = onPokemonClick,
        onBack = onBack,
        modifier = modifier
    )
}

// Stateless body: takes the already-collected LazyPagingItems and no ViewModel,
// so it shows in @Preview by feeding it a fixed PagingData source instead of a
// real Pager. Loading/error paging states aren't previewable this way (they
// come from an actual PagingSource's LoadState, not from PagingData.from), so
// only the loaded/populated case is covered here.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    items: LazyPagingItems<PokemonWithImage>,
    onPokemonClick: (PokemonWithImage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshState = items.loadState.refresh
    val snackbarHostState = remember { SnackbarHostState() }

    // A refresh that fails while the list is already populated is silent by design
    // (the list is kept). Surface that failure with a snackbar so the user knows
    // the refresh didn't happen, with a Retry action.
    val refreshFailedMessage = stringResource(R.string.error_refresh)
    val retryLabel = stringResource(R.string.retry)
    val refreshFailed = refreshState is LoadState.Error && items.itemCount > 0
    LaunchedEffect(refreshFailed) {
        if (refreshFailed) {
            val result = snackbarHostState.showSnackbar(
                message = refreshFailedMessage,
                actionLabel = retryLabel
            )
            if (result == SnackbarResult.ActionPerformed) items.refresh()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { PokemonListTopBar(onBack = onBack, onRefresh = items::refresh) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        // Full-screen loading/error only on the initial load (empty list). Once
        // there are items, a refresh shows as an overlay indicator and never
        // replaces the loaded content — a failed refresh keeps the list intact.
        when {
            items.itemCount == 0 && refreshState is LoadState.Loading ->
                LoadingIndicator(contentModifier.fillMaxSize())

            items.itemCount == 0 && refreshState is LoadState.Error ->
                RetryContent(
                    message = refreshState.error.message,
                    onRetry = items::retry,
                    modifier = contentModifier.fillMaxSize()
                )

            else -> PullToRefreshBox(
                isRefreshing = refreshState is LoadState.Loading,
                onRefresh = items::refresh,
                modifier = contentModifier.fillMaxSize()
            ) {
                PokemonList(items = items, onPokemonClick = onPokemonClick)
            }
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
                // Divider above every row except the first — separates items
                // without leaving a stray rule after the last one.
                if (index > 0) HorizontalDivider()
                PokemonRow(pokemon = pokemon, onClick = { onPokemonClick(pokemon) })
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

// Fabricated PagingData, no ViewModel/network — collectAsLazyPagingItems() on a
// fixed flow yields a fully loaded, non-refreshing LazyPagingItems, which is
// enough to preview the populated list.
private val previewPokemonList = listOf(
    PokemonWithImage(name = "bulbasaur", imageUrl = null),
    PokemonWithImage(name = "ivysaur", imageUrl = null),
    PokemonWithImage(name = "venusaur", imageUrl = null)
)

@Preview(showBackground = true)
@Composable
private fun PokemonListPreview() {
    val items = flowOf(PagingData.from(previewPokemonList)).collectAsLazyPagingItems()
    PokemonListScreen(items = items, onPokemonClick = {}, onBack = {})
}
