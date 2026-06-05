package pro.moreira.catapp.core.data.local.dao

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pro.moreira.catapp.core.data.local.CatAppDatabase

@RunWith(RobolectricTestRunner::class)
class BreedDaoTest {
    private lateinit var database: CatAppDatabase
    private lateinit var breedDao: BreedDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CatAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        breedDao = database.breedDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `search paging source filters breeds by name`() = runTest {
        breedDao.upsertBreeds(
            "abys" to "Abyssinian",
            "beng" to "Bengal",
            "sphy" to "Sphynx",
        )

        val result = breedDao.searchPagingSource("ben").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        )

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf("beng"), page.data.map { it.id })
    }

    @Test
    fun `remote upsert preserves favorite state`() = runTest {
        breedDao.upsertBreed(id = "abys", name = "Abyssinian")
        breedDao.toggleFavorite("abys")

        breedDao.upsertBreed(id = "abys", name = "Updated Abyssinian")

        val breed = checkNotNull(breedDao.getBreed("abys"))
        assertEquals("Updated Abyssinian", breed.name)
        assertTrue(breed.isFavorite)
    }

    @Test
    fun `toggle favorite flips state`() = runTest {
        breedDao.upsertBreed(id = "abys", name = "Abyssinian")

        assertEquals(1, breedDao.toggleFavorite("abys"))
        assertEquals(true, breedDao.getBreed("abys")?.isFavorite)

        assertEquals(1, breedDao.toggleFavorite("abys"))
        assertEquals(false, breedDao.getBreed("abys")?.isFavorite)
    }

    @Test
    fun `observe favorites returns only favorited breeds`() = runTest {
        breedDao.upsertBreeds(
            "abys" to "Abyssinian",
            "beng" to "Bengal",
            "sphy" to "Sphynx",
        )
        breedDao.favorite("abys", "sphy")

        val favorites = breedDao.observeFavorites().first()

        assertEquals(listOf("abys", "sphy"), favorites.map { it.id }.sorted())
    }

    @Test
    fun `observe favorites returns breeds ordered by name`() = runTest {
        breedDao.upsertBreeds(
            "sphy" to "Sphynx",
            "beng" to "Bengal",
            "abys" to "Abyssinian",
        )
        breedDao.favorite("abys", "beng", "sphy")

        val favorites = breedDao.observeFavorites().first()

        assertEquals(
            listOf("Abyssinian", "Bengal", "Sphynx"),
            favorites.map { it.name },
        )
    }

    private suspend fun BreedDao.upsertBreeds(vararg breeds: Pair<String, String>) {
        breeds.forEach { (id, name) -> upsertBreed(id = id, name = name) }
    }

    private suspend fun BreedDao.favorite(vararg ids: String) {
        ids.forEach { id -> toggleFavorite(id) }
    }

    private suspend fun BreedDao.upsertBreed(
        id: String,
        name: String,
    ) {
        upsertRemoteFieldsPreservingFavorite(
            id = id,
            name = name,
            imageUrl = null,
            origin = "",
            temperament = "",
            description = "",
            lifespan = "",
        )
    }
}
