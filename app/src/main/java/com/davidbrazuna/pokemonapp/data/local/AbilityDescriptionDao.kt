package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AbilityDescriptionDao {

    @Query("SELECT * FROM ability_description WHERE name = :name")
    suspend fun get(name: String): AbilityDescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AbilityDescriptionEntity)
}
