package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.davidbrazuna.pokemonapp.R
import com.davidbrazuna.pokemonapp.model.Ability
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.viewmodel.DetailUiState
import com.davidbrazuna.pokemonapp.viewmodel.PokemonDetailViewModel

@Composable
fun PokemonDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DetailUiState.Loading -> CenterBox(modifier) { CircularProgressIndicator() }
        is DetailUiState.Error -> CenterBox(modifier) {
            RetryContent(message = state.message, onRetry = viewModel::retry)
        }
        is DetailUiState.Success -> PokemonDetailContent(state.pokemon, modifier)
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetailResponseData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val placeholder = ColorPainter(Color.LightGray)
            AsyncImage(
                model = pokemon.sprites.other?.home?.frontDefault,
                contentDescription = pokemon.name,
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(200.dp)
            )
        }
        item { DetailText(stringResource(R.string.detail_id, pokemon.id)) }
        item { DetailText(stringResource(R.string.detail_name, pokemon.name)) }
        item { DetailText(stringResource(R.string.detail_height, pokemon.height)) }
        item { DetailText(stringResource(R.string.detail_weight, pokemon.weight)) }
        item {
            val baseExp = pokemon.baseExperience?.toString()
                ?: stringResource(R.string.detail_base_experience_unknown)
            DetailText(stringResource(R.string.detail_base_experience, baseExp))
        }
        item { DetailText(stringResource(R.string.detail_abilities_title)) }
        items(pokemon.abilities) { ability ->
            AbilityRow(ability)
        }
    }
}

@Composable
private fun DetailText(text: String) {
    Text(text = text, fontSize = 25.sp)
}

@Composable
private fun AbilityRow(ability: Ability) {
    Text(
        text = ability.ability.name,
        fontSize = 25.sp,
        modifier = Modifier.padding(start = 10.dp)
    )
}

@Composable
private fun CenterBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun RetryContent(message: String?, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message ?: stringResource(R.string.error_generic),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}
