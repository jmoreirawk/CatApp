package pro.moreira.catapp.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import pro.moreira.catapp.core.data.local.CatAppDatabase
import pro.moreira.catapp.core.data.local.dao.BreedDao
import pro.moreira.catapp.core.data.local.dao.BreedRemoteKeyDao
import pro.moreira.catapp.core.data.local.dao.CacheMetadataDao
import pro.moreira.catapp.core.data.local.entity.BreedEntity
import pro.moreira.catapp.core.data.local.entity.BreedRemoteKeyEntity
import pro.moreira.catapp.core.data.local.entity.CacheMetadataEntity
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.mapper.toEntity
import pro.moreira.catapp.core.data.time.TimeProvider
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
internal class BreedRemoteMediator(
    private val query: String,
    private val api: CatApiService,
    private val database: CatAppDatabase,
    private val timeProvider: TimeProvider,
    private val cacheFreshnessMillis: Long = CACHE_FRESHNESS_MILLIS,
) : RemoteMediator<Int, BreedEntity>() {
    private val breedDao: BreedDao = database.breedDao()
    private val remoteKeyDao: BreedRemoteKeyDao = database.breedRemoteKeyDao()
    private val cacheMetadataDao: CacheMetadataDao = database.cacheMetadataDao()

    override suspend fun initialize(): InitializeAction {
        val lastUpdatedAtMillis = cacheMetadataDao.lastUpdatedAtMillis(query)
            ?: return InitializeAction.LAUNCH_INITIAL_REFRESH

        return if (isCacheFresh(
                lastUpdatedAtMillis = lastUpdatedAtMillis,
                nowMillis = timeProvider.nowMillis(),
                freshnessMillis = cacheFreshnessMillis,
            )
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BreedEntity>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> FIRST_PAGE
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> remoteKeyDao.nextPageForAppend(query)
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }
        val pageSize = state.config.pageSize

        return try {
            val breeds = api.getBreeds(page = page, limit = pageSize)
            val endOfPaginationReached = breeds.size < pageSize

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteForQuery(query)
                }

                val positionOffset = page * pageSize
                val nextPage = (page + 1).takeUnless { endOfPaginationReached }
                val keys = breeds.mapIndexed { index, breed ->
                    BreedRemoteKeyEntity(
                        query = query,
                        breedId = breed.id,
                        nextPage = nextPage,
                        position = positionOffset + index,
                    )
                }

                breeds
                    .map { it.toEntity() }
                    .forEach { breed ->
                        breedDao.upsertRemoteFieldsPreservingFavorite(
                            id = breed.id,
                            name = breed.name,
                            imageUrl = breed.imageUrl,
                            origin = breed.origin,
                            temperament = breed.temperament,
                            description = breed.description,
                            lifespan = breed.lifespan,
                        )
                    }

                remoteKeyDao.upsertAll(keys)
                cacheMetadataDao.upsert(
                    CacheMetadataEntity(
                        query = query,
                        lastUpdatedAtMillis = timeProvider.nowMillis(),
                    ),
                )
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (error: Exception) {
            if (error is CancellationException) {
                throw error
            }
            MediatorResult.Error(error)
        }
    }

    internal companion object {
        const val DEFAULT_QUERY = "breeds"
        const val FIRST_PAGE = 0
        val CACHE_FRESHNESS_MILLIS: Long = TimeUnit.HOURS.toMillis(1)

        fun isCacheFresh(
            lastUpdatedAtMillis: Long,
            nowMillis: Long,
            freshnessMillis: Long,
        ): Boolean = nowMillis - lastUpdatedAtMillis < freshnessMillis
    }
}
