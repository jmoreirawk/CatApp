package pro.moreira.catapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
internal data class CacheMetadataEntity(
    @PrimaryKey val query: String,
    val lastUpdatedAtMillis: Long,
)
