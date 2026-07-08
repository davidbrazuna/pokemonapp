package com.davidbrazuna.pokemonapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.davidbrazuna.pokemonapp.viewmodel.AbilityDescriptionUiState
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
    val selectedAbilityIndex by viewModel.selectedAbilityIndex.collectAsStateWithLifecycle()
    val abilityDescriptionState by viewModel.abilityDescriptionState.collectAsStateWithLifecycle()
    PokemonDetailScreen(
        title = viewModel.pokemonName.capitalizeForDisplay(),
        uiState = uiState,
        selectedAbilityIndex = selectedAbilityIndex,
        abilityDescriptionState = abilityDescriptionState,
        onAbilitySelected = viewModel::selectAbility,
        onRetryAbilityDescription = viewModel::retryAbilityDescription,
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
    selectedAbilityIndex: Int,
    abilityDescriptionState: AbilityDescriptionUiState,
    onAbilitySelected: (Int) -> Unit,
    onRetryAbilityDescription: () -> Unit,
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
            is DetailUiState.Success -> PokemonDetailContent(
                pokemon = uiState.pokemon,
                selectedAbilityIndex = selectedAbilityIndex,
                abilityDescriptionState = abilityDescriptionState,
                onAbilitySelected = onAbilitySelected,
                onRetryAbilityDescription = onRetryAbilityDescription,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetailResponseData,
    selectedAbilityIndex: Int,
    abilityDescriptionState: AbilityDescriptionUiState,
    onAbilitySelected: (Int) -> Unit,
    onRetryAbilityDescription: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { PokemonHeader(pokemon, Modifier.padding(top = 16.dp)) }
        item { StatsRow(pokemon, Modifier.padding(horizontal = 16.dp)) }
        item {
            // Title and chips share one item with tighter spacing (8dp) than the
            // LazyColumn's default 24dp between sections — keeps the label close
            // to what it labels instead of reading as its own separate block.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Extra breathing room above this section specifically, on
                    // top of the LazyColumn's normal 24dp gap from StatsRow.
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(text = stringResource(R.string.detail_abilities_title))
                AbilitiesRow(
                    abilities = pokemon.abilities,
                    selectedIndex = selectedAbilityIndex,
                    onAbilitySelected = onAbilitySelected
                )
            }
        }
        item {
            AbilityDescription(
                state = abilityDescriptionState,
                onRetry = onRetryAbilityDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

// Sprite on a circular tonal background (instead of floating on the plain
// page background), name and "#id" below — a proper header instead of the
// old "id: 2" / "name: ivysaur" raw data lines.
@Composable
private fun PokemonHeader(pokemon: PokemonDetailResponseData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            PokemonSprite(
                imageUrl = pokemon.sprites.other?.home?.frontDefault,
                contentDescription = pokemon.name,
                modifier = Modifier.size(160.dp)
            )
        }
        Text(
            text = pokemon.name.capitalizeForDisplay(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(R.string.detail_id_badge, pokemon.id),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Height/weight/base experience as three small stat cards side by side,
// instead of separate raw-data text lines. The API reports height in
// decimetres and weight in hectograms; converted to metres/kilograms here.
@Composable
private fun StatsRow(pokemon: PokemonDetailResponseData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = stringResource(R.string.detail_stat_height_label),
            value = stringResource(R.string.detail_height_value, pokemon.height / 10f),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.detail_stat_weight_label),
            value = stringResource(R.string.detail_weight_value, pokemon.weight / 10f),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.detail_stat_base_experience_label),
            value = pokemon.baseExperience?.toString()
                ?: stringResource(R.string.detail_base_experience_unknown),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// A section header ("Abilities") — semantically distinct from the stat cards
// and the chip row below it.
@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}

// Single-select row: exactly one ability is selected at all times (index 0 by
// default, set by the ViewModel as soon as the Pokemon loads), so this is a
// FilterChip group rather than a row of independent action chips.
@Composable
private fun AbilitiesRow(
    abilities: List<Ability>,
    selectedIndex: Int,
    onAbilitySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        // A Pokemon can have 2-3 abilities (including a hidden one, which reads
        // longer with its "(hidden)" suffix); a plain Row neither wraps nor
        // scrolls, so on narrow screens the trailing chip(s) were clipped at the
        // edge — not just visually, but untappable, making that ability's
        // description unreachable.
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        abilities.forEachIndexed { index, ability ->
            AbilityFilterChip(
                ability = ability,
                selected = index == selectedIndex,
                onClick = { onAbilitySelected(index) }
            )
        }
    }
}

// Selected = violet (secondaryContainer), unselected = white with an outline —
// FilterChip's own selected/unselected look, not simulated manually. The
// hidden ability is marked with a "(hidden)" suffix instead of a separate
// color/border, since fill vs. outline is now spoken for by selection state.
@Composable
private fun AbilityFilterChip(
    ability: Ability,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rawLabel = ability.ability.name.capitalizeForDisplay()
    val label = if (ability.isHidden) {
        stringResource(R.string.detail_ability_hidden_label, rawLabel)
    } else {
        rawLabel
    }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
    )
}

// Loading/Success/Error for the selected ability's description — the same
// three-state shape used elsewhere in the app, just scoped to this one section
// instead of the whole screen.
@Composable
private fun AbilityDescription(
    state: AbilityDescriptionUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is AbilityDescriptionUiState.Loading ->
            LoadingIndicator(modifier.padding(vertical = 16.dp))

        is AbilityDescriptionUiState.Error ->
            RetryContent(
                message = state.message,
                onRetry = onRetry,
                modifier = modifier.padding(vertical = 16.dp)
            )

        is AbilityDescriptionUiState.Success ->
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier
            )
    }
}

// Previews exercise the stateless body with fabricated state — no ViewModel,
// no network. This is the payoff of the stateful/stateless split.
private fun previewAbility(name: String, isHidden: Boolean = false) =
    Ability(ability = AbilityItem(name = name, url = ""), isHidden = isHidden, slot = 1)

private val previewPokemon = PokemonDetailResponseData(
    id = 2,
    name = "ivysaur",
    height = 10,
    weight = 130,
    baseExperience = 142,
    abilities = listOf(
        previewAbility("overgrow"),
        previewAbility("chlorophyll", isHidden = true)
    ),
    sprites = Sprites()
)

@Preview(showBackground = true)
@Composable
private fun PokemonDetailSuccessPreview() {
    PokemonDetailScreen(
        title = "Ivysaur",
        uiState = DetailUiState.Success(previewPokemon),
        selectedAbilityIndex = 0,
        abilityDescriptionState = AbilityDescriptionUiState.Success(
            "Prevents the Pokémon from falling asleep."
        ),
        onAbilitySelected = {},
        onRetryAbilityDescription = {},
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
        selectedAbilityIndex = 0,
        abilityDescriptionState = AbilityDescriptionUiState.Loading,
        onAbilitySelected = {},
        onRetryAbilityDescription = {},
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
        selectedAbilityIndex = 0,
        abilityDescriptionState = AbilityDescriptionUiState.Loading,
        onAbilitySelected = {},
        onRetryAbilityDescription = {},
        onRetry = {},
        onBack = {},
        onHome = {}
    )
}
