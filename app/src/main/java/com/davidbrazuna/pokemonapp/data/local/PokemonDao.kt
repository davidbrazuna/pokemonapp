package com.davidbrazuna.pokemonapp.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PokemonDao {

    // Room generates the PagingSource from this query; ordering by id keeps the
    // list in the same order the API paginates (see PokemonEntity).
    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, PokemonEntity>

    // REPLACE so a REFRESH re-inserting the same ids updates them in place rather
    // than throwing on the primary key.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemon: List<PokemonEntity>)

    @Query("DELETE FROM pokemon")
    suspend fun clearAll()
}
