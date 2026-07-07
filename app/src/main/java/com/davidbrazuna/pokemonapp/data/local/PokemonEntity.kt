package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Flattened, offline-persistable version of the list's UI model (PokemonWithImage).
// id is the PokeAPI id, used as the primary key and as the row order for paging:
// the API returns Pokemon in id order, so ordering the local PagingSource by id
// preserves the same sequence the RemoteMediator appended them in.
@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String?
)
