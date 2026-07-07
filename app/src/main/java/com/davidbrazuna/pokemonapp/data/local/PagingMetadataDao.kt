package com.davidbrazuna.pokemonapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PagingMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: PagingMetadataEntity)

    @Query("SELECT * FROM paging_metadata WHERE id = :id")
    suspend fun get(id: Int = PagingMetadataEntity.SINGLETON_ID): PagingMetadataEntity?

    @Query("DELETE FROM paging_metadata")
    suspend fun clear()
}
