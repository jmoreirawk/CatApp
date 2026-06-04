package pro.moreira.catapp.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pro.moreira.catapp.core.data.local.CatAppDatabase
import pro.moreira.catapp.core.data.local.dao.BreedDao
import pro.moreira.catapp.core.data.local.dao.BreedRemoteKeyDao
import pro.moreira.catapp.core.data.local.dao.CacheMetadataDao
import pro.moreira.catapp.core.data.time.SystemTimeProvider
import pro.moreira.catapp.core.data.time.TimeProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideCatAppDatabase(
        @ApplicationContext context: Context,
    ): CatAppDatabase = Room.databaseBuilder(
        context = context,
        klass = CatAppDatabase::class.java,
        name = "cat-app.db",
    ).build()

    @Provides
    fun provideBreedDao(database: CatAppDatabase): BreedDao = database.breedDao()

    @Provides
    fun provideBreedRemoteKeyDao(database: CatAppDatabase): BreedRemoteKeyDao = database.breedRemoteKeyDao()

    @Provides
    fun provideCacheMetadataDao(database: CatAppDatabase): CacheMetadataDao = database.cacheMetadataDao()

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = SystemTimeProvider
}
