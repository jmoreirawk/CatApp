package pro.moreira.catapp.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import pro.moreira.catapp.core.data.paging.BreedsPagingSource
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.mapper.toDomain
import pro.moreira.catapp.core.domain.model.Breed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreedRepository @Inject internal constructor(
    private val api: CatApiService,
) {
    fun getBreeds(): Flow<PagingData<Breed>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { BreedsPagingSource(api) },
    ).flow

    suspend fun getBreed(id: String): Breed = api.getBreed(id).toDomain()

    private companion object {
        const val PAGE_SIZE = 20
    }
}
