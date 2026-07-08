package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidbrazuna.pokemonapp.R
import com.davidbrazuna.pokemonapp.model.Ability
import com.davidbrazuna.pokemonapp.model.AbilityItem
import com.davidbrazuna.pokemonapp.model.PokemonDetailResponseData
import com.davidbrazuna.pokemonapp.model.Sprites
import com.davidbrazuna.pokemonapp.ui.components.LoadingIndicator
import com.davidbrazuna.pokemonapp.ui.components.PokemonDetailTopBar
import com.davidbrazuna.pokemonapp.ui.components.PokemonSprite
import com.davidbrazuna.pokemonapp.ui.components.RetryContent
import com.davidbrazuna.pokemonapp.viewmodel.DetailUiState
import com.davidbrazuna.pokemonapp.viewmodel.PokemonDetailViewModel

// Stateful entry point: binds the ViewModel and delegates to the stateless body.
// The top-bar title is derived here from the ViewModel's name (which comes from
// the route arg via SavedStateHandle), so the nav layer doesn't format it.
@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonDetailScreen(
        title = viewModel.pokemonName.capitalizeForDisplay(),
        uiState = uiState,
        onRetry = viewModel::retry,
        onBack = onBack,
        onHome = onHome,
        modifier = modifier
    )
}

// Stateless body: takes state + callbacks and no ViewModel, so the Scaffold + top
// bar show in @Preview and it is testable with DetailUiState.Success/Error/Loading.
@Composable
fun PokemonDetailScreen(
    title: String,
    uiState: DetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { PokemonDetailTopBar(title = title, onBack = onBack, onHome = onHome) }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding).fillMaxSize()
        when (uiState) {
            is DetailUiState.Loading -> LoadingIndicator(contentModifier)
            is DetailUiState.Error -> RetryContent(
                message = uiState.message,
                onRetry = onRetry,
                modifier = contentModifier
            )
            is DetailUiState.Success -> PokemonDetailContent(uiState.pokemon, contentModifier)
        }
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
            PokemonSprite(
                imageUrl = pokemon.sprites.other?.home?.frontDefault,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(200.dp)
            )
        }
        item { DetailField(stringResource(R.string.detail_id, pokemon.id)) }
        item { DetailField(stringResource(R.string.detail_name, pokemon.name.capitalizeForDisplay())) }
        item { DetailField(stringResource(R.string.detail_height, pokemon.height)) }
        item { DetailField(stringResource(R.string.detail_weight, pokemon.weight)) }
        item {
            val baseExp = pokemon.baseExperience?.toString()
                ?: stringResource(R.string.detail_base_experience_unknown)
            DetailField(stringResource(R.string.detail_base_experience, baseExp))
        }
        item { SectionTitle(stringResource(R.string.detail_abilities_title)) }
        items(pokemon.abilities) { ability ->
            AbilityRow(ability)
        }
    }
}

// A single data line (id, name, height, …). bodyLarge scales with the theme.
@Composable
private fun DetailField(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = modifier)
}

// A section header ("Abilities:") — semantically distinct from a data line.
@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

@Composable
private fun AbilityRow(ability: Ability, modifier: Modifier = Modifier) {
    Text(
        text = ability.ability.name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

// Previews exercise the stateless body with fabricated state — no ViewModel,
// no network. This is the payoff of the stateful/stateless split.
private fun previewAbility(name: String) =
    Ability(ability = AbilityItem(name = name, url = ""), isHidden = false, slot = 1)

private val previewPokemon = PokemonDetailResponseData(
    id = 2,
    name = "ivysaur",
    height = 10,
    weight = 130,
    baseExperience = 142,
    abilities = listOf(previewAbility("overgrow"), previewAbility("chlorophyll")),
    sprites = Sprites()
)

@Preview(showBackground = true)
@Composable
private fun PokemonDetailSuccessPreview() {
    PokemonDetailScreen(
        title = "Ivysaur",
        uiState = DetailUiState.Success(previewPokemon),
        onRetry = {},
        onBack = {},
        onHome = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailErrorPreview() {
    PokemonDetailScreen(
        title = "Ivysaur",
        uiState = DetailUiState.Error("Unable to resolve host"),
        onRetry = {},
        onBack = {},
        onHome = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailLoadingPreview() {
    PokemonDetailScreen(
        title = "Ivysaur",
        uiState = DetailUiState.Loading,
        onRetry = {},
        onBack = {},
        onHome = {}
    )
}
