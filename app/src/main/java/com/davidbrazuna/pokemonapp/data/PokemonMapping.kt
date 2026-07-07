package com.davidbrazuna.pokemonapp.data

import com.davidbrazuna.pokemonapp.data.local.PokemonEntity
import com.davidbrazuna.pokemonapp.model.Pokemon
import com.davidbrazuna.pokemonapp.model.PokemonWithImage

// List item urls look like https://pokeapi.co/api/v2/pokemon/{id}/ — the id is
// the last non-empty path segment. The official sprite repo is keyed by that id,
// so both the primary key and the sprite URL are derived from it (this is the
// N+1 fix: no per-Pokemon detail call).
internal fun Pokemon.idFromUrl(): Int? =
    url.trimEnd('/').substringAfterLast('/').toIntOrNull()

internal fun spriteUrlForId(id: Int): String =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

// API model -> Room entity. Returns null when the id can't be parsed (the entity
// needs it as a primary key), so callers drop such items.
internal fun Pokemon.toEntity(): PokemonEntity? {
    val id = idFromUrl() ?: return null
    return PokemonEntity(id = id, name = name, imageUrl = spriteUrlForId(id))
}

// Room entity -> UI model consumed by the list (unchanged from before Room).
internal fun PokemonEntity.toUiModel(): PokemonWithImage =
    PokemonWithImage(name = name, imageUrl = imageUrl)
