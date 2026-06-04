package pro.moreira.catapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pro.moreira.catapp.core.data.local.entity.CacheMetadataEntity

@Dao
internal interface CacheMetadataDao {
    @Query("SELECT lastUpdatedAtMillis FROM cache_metadata WHERE query = :query")
    suspend fun lastUpdatedAtMillis(query: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: CacheMetadataEntity)
}
