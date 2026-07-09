package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Single-row table (id is always SINGLETON_ID) holding the mediator's pagination
// state. The PokeAPI only paginates forward, so one scalar — the offset of the
// next page to append, derived from the API's own `next` link — is all that's
// needed; there is no per-item / bidirectional key to track.
//
// nextOffset == null means the last page has been reached (no further append).
// lastUpdated backs the initialize() TTL so an online cold start doesn't wipe a
// fresh cache down to page 1.
@Entity(tableName = "paging_metadata")
data class PagingMetadataEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val nextOffset: Int?,
    val lastUpdated: Long
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
