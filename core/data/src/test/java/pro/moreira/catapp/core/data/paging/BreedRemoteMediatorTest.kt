package pro.moreira.catapp.core.data.paging

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pro.moreira.catapp.core.data.local.CatAppDatabase
import pro.moreira.catapp.core.data.local.entity.BreedEntity
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.dto.BreedDto
import pro.moreira.catapp.core.data.time.TimeProvider

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class BreedRemoteMediatorTest {
    private lateinit var database: CatAppDatabase
    private lateinit var api: FakeCatApiService
    private val timeProvider = object : TimeProvider {
        override fun nowMillis(): Long = NOW_MILLIS
    }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CatAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = FakeCatApiService()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `fresh cache skips initial refresh`() {
        val now = 10_000L
        val freshness = 1_000L

        assertEquals(
            true,
            BreedRemoteMediator.isCacheFresh(
                lastUpdatedAtMillis = now - freshness + 1,
                nowMillis = now,
                freshnessMillis = freshness,
            ),
        )
    }

    @Test
    fun `stale cache launches initial refresh`() {
        val now = 10_000L
        val freshness = 1_000L

        assertEquals(
            false,
            BreedRemoteMediator.isCacheFresh(
                lastUpdatedAtMillis = now - freshness,
                nowMillis = now,
                freshnessMillis = freshness,
            ),
        )
    }

    @Test
    fun `refresh caches breeds and remote keys`() = runTest {
        api.page(0, breed("abys", "Abyssinian"), breed("beng", "Bengal"))

        val result = mediator().load(LoadType.REFRESH, pagingState())

        assertEquals(false, result.endOfPaginationReached())
        assertEquals("Abyssinian", database.breedDao().getBreed("abys")?.name)
        assertEquals(1, database.breedRemoteKeyDao().nextPageForAppend(QUERY))
        assertEquals(NOW_MILLIS, database.cacheMetadataDao().lastUpdatedAtMillis(QUERY))
    }

    @Test
    fun `append loads next page until end of list`() = runTest {
        api.page(0, breed("abys", "Abyssinian"), breed("beng", "Bengal"))
        api.page(1, breed("sphy", "Sphynx"))

        mediator().load(LoadType.REFRESH, pagingState())
        val result = mediator().load(LoadType.APPEND, pagingState())

        assertEquals(true, result.endOfPaginationReached())
        assertEquals("Sphynx", database.breedDao().getBreed("sphy")?.name)
        assertEquals(null, database.breedRemoteKeyDao().nextPageForAppend(QUERY))
    }

    @Test
    fun `refresh reports end of pagination when remote returns fewer than page size`() = runTest {
        api.page(0, breed("abys", "Abyssinian"))

        val result = mediator().load(LoadType.REFRESH, pagingState())

        assertEquals(true, result.endOfPaginationReached())
        assertEquals(null, database.breedRemoteKeyDao().nextPageForAppend(QUERY))
    }

    @Test
    fun `refresh returns error when remote load fails`() = runTest {
        api.error = RuntimeException("Network error")

        val result = mediator().load(LoadType.REFRESH, pagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

    private fun mediator() = BreedRemoteMediator(
        query = QUERY,
        api = api,
        database = database,
        timeProvider = timeProvider,
    )

    private fun pagingState() = PagingState<Int, BreedEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = PAGE_SIZE),
        leadingPlaceholderCount = 0,
    )

    private fun breed(id: String, name: String) = BreedDto(id = id, name = name)

    private fun RemoteMediator.MediatorResult.endOfPaginationReached(): Boolean =
        (this as RemoteMediator.MediatorResult.Success).endOfPaginationReached

    private companion object {
        const val QUERY = "breeds"
        const val PAGE_SIZE = 2
        const val NOW_MILLIS = 10_000L
    }

    private class FakeCatApiService : CatApiService {
        val breedsByPage = mutableMapOf<Int, List<BreedDto>>()
        var error: Exception? = null

        fun page(page: Int, vararg breeds: BreedDto) {
            breedsByPage[page] = breeds.toList()
        }

        override suspend fun getBreeds(
            page: Int,
            limit: Int,
            order: String,
        ): List<BreedDto> {
            error?.let { throw it }
            return breedsByPage[page].orEmpty()
        }

        override suspend fun getBreed(breedId: String): BreedDto {
            error?.let { throw it }
            return BreedDto(id = breedId, name = breedId)
        }
    }
}
