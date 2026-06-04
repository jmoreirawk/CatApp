package pro.moreira.catapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import pro.moreira.catapp.core.data.local.dao.BreedDao
import pro.moreira.catapp.core.data.local.dao.BreedRemoteKeyDao
import pro.moreira.catapp.core.data.local.dao.CacheMetadataDao
import pro.moreira.catapp.core.data.local.entity.BreedEntity
import pro.moreira.catapp.core.data.local.entity.BreedRemoteKeyEntity
import pro.moreira.catapp.core.data.local.entity.CacheMetadataEntity

@Database(
    entities = [
        BreedEntity::class,
        BreedRemoteKeyEntity::class,
        CacheMetadataEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
internal abstract class CatAppDatabase : RoomDatabase() {
    abstract fun breedDao(): BreedDao
    abstract fun breedRemoteKeyDao(): BreedRemoteKeyDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
}
