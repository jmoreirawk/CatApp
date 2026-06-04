package pro.moreira.catapp.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.mapper.toDomain
import pro.moreira.catapp.core.domain.model.Breed
import javax.inject.Inject

internal class BreedsPagingSource @Inject constructor(
    private val api: CatApiService,
) : PagingSource<Int, Breed>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Breed> {
        val page = params.key ?: FIRST_PAGE

        return try {
            val breeds = api.getBreeds(page = page, limit = PAGE_SIZE).map { it.toDomain() }
            LoadResult.Page(
                data = breeds,
                prevKey = page.takeIf { it > FIRST_PAGE }?.minus(1),
                nextKey = (page + 1).takeIf { breeds.size == PAGE_SIZE },
            )
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Breed>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    private companion object {
        const val FIRST_PAGE = 0
        const val PAGE_SIZE = 20
    }
}
