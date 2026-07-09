package com.davidbrazuna.pokemonapp.data

import androidx.room.withTransaction
import com.davidbrazuna.pokemonapp.data.local.PokemonDatabase

// Seam over Room's withTransaction so the mediator's transactional block can be
// unit-tested without a real database. The generic return type mirrors
// withTransaction's, so a block that returns a value keeps working.
interface Transactor {
    suspend fun <R> run(block: suspend () -> R): R
}

// Production implementation: a genuine Room transaction (atomic + rolled back on
// throw). The rollback behaviour is exercised in instrumented tests, not here.
class RoomTransactor(private val database: PokemonDatabase) : Transactor {
    override suspend fun <R> run(block: suspend () -> R): R =
        database.withTransaction(block)
}
