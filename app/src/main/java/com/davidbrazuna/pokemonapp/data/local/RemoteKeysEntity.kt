package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// RemoteMediator bookkeeping: for each cached Pokemon, the API offsets of the
// previous and next pages. The mediator reads these off the first/last cached
// item to know which page to request on PREPEND/APPEND without recomputing
// offsets from item counts. Keys are nullable: null means "no page in that
// direction" (endOfPaginationReached).
@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey val pokemonId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
