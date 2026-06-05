package pro.moreira.catapp.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pro.moreira.catapp.core.data.local.CatAppDatabase
import pro.moreira.catapp.core.data.local.dao.BreedDao
import pro.moreira.catapp.core.data.local.mapper.toDomain
import pro.moreira.catapp.core.data.paging.BreedRemoteMediator
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.mapper.toDomain
import pro.moreira.catapp.core.data.remote.mapper.toEntity
import pro.moreira.catapp.core.data.time.TimeProvider
import pro.moreira.catapp.core.domain.model.Breed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreedRepository
@Inject
internal constructor(
    private val api: CatApiService,
    private val database: CatAppDatabase,
    private val breedDao: BreedDao,
    private val timeProvider: TimeProvider,
) {
    fun getBreeds(query: String): Flow<PagingData<Breed>> {
        val trimmedQuery = query.trim()
        return if (trimmedQuery.isEmpty()) {
            defaultBreedPager()
        } else {
            searchBreedPager(trimmedQuery)
        }.flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun defaultBreedPager() = Pager(
        config =
            PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
        remoteMediator =
            BreedRemoteMediator(
                query = BreedRemoteMediator.DEFAULT_QUERY,
                api = api,
                database = database,
                timeProvider = timeProvider,
            ),
        pagingSourceFactory = { breedDao.pagingSource(BreedRemoteMediator.DEFAULT_QUERY) },
    )

    private fun searchBreedPager(query: String) =
        Pager(
            config =
                PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    enablePlaceholders = false,
                ),
            pagingSourceFactory = { breedDao.searchPagingSource(query) },
        )

    suspend fun getBreed(id: String): Breed {
        val cachedBreed = breedDao.getBreed(id)
        if (cachedBreed != null) {
            return cachedBreed.toDomain()
        }

        val remoteBreed = api.getBreed(id)
        val entity = remoteBreed.toEntity()
        breedDao.upsertRemoteFieldsPreservingFavorite(
            id = entity.id,
            name = entity.name,
            imageUrl = entity.imageUrl,
            origin = entity.origin,
            temperament = entity.temperament,
            description = entity.description,
            lifespan = entity.lifespan,
        )
        return remoteBreed.toDomain()
    }

    suspend fun toggleFavorite(breedId: String) {
        check(breedDao.toggleFavorite(breedId) > 0)
    }

    fun observeFavoriteBreeds(): Flow<List<Breed>> =
        breedDao.observeFavorites().map { entities -> entities.map { it.toDomain() } }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
