package pro.moreira.catapp.core.data.paging

import androidx.paging.PagingSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.dto.BreedDto
import pro.moreira.catapp.core.data.remote.mapper.toDomain

class BreedsPagingSourceTest {
    @Test
    fun `initial page starts at zero and requests the configured page size`() = runTest {
        val api = FakeCatApiService(response = breeds(20))

        val result = BreedsPagingSource(api).load(refresh())

        assertEquals(listOf(Request(page = 0, limit = 20, order = "ASC")), api.requests)
        assertEquals(
            PagingSource.LoadResult.Page(
                data = breeds(20).map { it.toDomain() },
                prevKey = null,
                nextKey = 1,
            ),
            result,
        )
    }

    @Test
    fun `subsequent page advances and points back to previous page`() = runTest {
        val api = FakeCatApiService(response = breeds(20))

        val result = BreedsPagingSource(api).load(append(key = 2))

        assertEquals(
            PagingSource.LoadResult.Page(
                data = breeds(20).map { it.toDomain() },
                prevKey = 1,
                nextKey = 3,
            ),
            result,
        )
    }

    @Test
    fun `short page marks the end of pagination`() = runTest {
        val api = FakeCatApiService(response = breeds(3))

        val result = BreedsPagingSource(api).load(append(key = 1))

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
    }

    @Test
    fun `network failure remains observable`() = runTest {
        val failure = IllegalStateException("network failure")
        val api = FakeCatApiService(failure = failure)

        val result = BreedsPagingSource(api).load(refresh())

        assertSame(failure, (result as PagingSource.LoadResult.Error).throwable)
    }

    private fun refresh() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = 60,
        placeholdersEnabled = false,
    )

    private fun append(key: Int) = PagingSource.LoadParams.Append(
        key = key,
        loadSize = 20,
        placeholdersEnabled = false,
    )

    private fun breeds(count: Int) = List(count) { index ->
        BreedDto(id = "$index", name = "Breed $index")
    }
}

private data class Request(
    val page: Int,
    val limit: Int,
    val order: String,
)

private class FakeCatApiService(
    private val response: List<BreedDto> = emptyList(),
    private val failure: Exception? = null,
) : CatApiService {
    val requests = mutableListOf<Request>()

    override suspend fun getBreeds(page: Int, limit: Int, order: String): List<BreedDto> {
        requests += Request(page, limit, order)
        failure?.let { throw it }
        return response
    }

    override suspend fun getBreed(breedId: String): BreedDto = error("Not used")
}
