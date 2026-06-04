package pro.moreira.catapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pro.moreira.catapp.core.data.local.entity.BreedRemoteKeyEntity

@Dao
internal interface BreedRemoteKeyDao {
    @Query("SELECT nextPage FROM breed_remote_keys WHERE query = :query ORDER BY position DESC LIMIT 1")
    suspend fun nextPageForAppend(query: String): Int?

    @Query("DELETE FROM breed_remote_keys WHERE query = :query")
    suspend fun deleteForQuery(query: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(keys: List<BreedRemoteKeyEntity>)
}
