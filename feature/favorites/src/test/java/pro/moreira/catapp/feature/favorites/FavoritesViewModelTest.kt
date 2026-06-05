package pro.moreira.catapp.feature.favorites

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.Mockito.doThrow
import pro.moreira.catapp.core.data.repository.BreedRepository
import pro.moreira.catapp.core.domain.model.Breed

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val repository = mock<BreedRepository>()
    private val favoriteBreeds = MutableSharedFlow<List<Breed>>()
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(repository.observeFavoriteBreeds()).thenReturn(favoriteBreeds)
        viewModel = FavoritesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `favoriteBreeds is null before first repository emission`() = runTest(testDispatcher) {
        assertNull(viewModel.favoriteBreeds.value)
    }

    @Test
    fun `favoriteBreeds emits empty list after repository emits no favorites`() = runTest(testDispatcher) {
        val collectJob = collectFavoriteBreeds()

        emitFavorites(emptyList())

        assertEquals(emptyList<Breed>(), viewModel.favoriteBreeds.value)
        collectJob.cancel()
    }

    @Test
    fun `averageLifespan updates from favorite breeds`() = runTest(testDispatcher) {
        val collectJob = collectAverageLifespan()

        emitFavorites(
            breed(id = "abys", lifespan = "14 - 15"),
            breed(id = "aege", lifespan = "9 - 12"),
            breed(id = "bad", lifespan = "unknown"),
        )

        assertEquals(11.5, viewModel.averageLifespan.value!!, 0.001)
        collectJob.cancel()
    }

    @Test
    fun `averageLifespan is null when no favorite has a parseable lifespan`() = runTest(testDispatcher) {
        val collectJob = collectAverageLifespan()

        emitFavorites(breed(id = "bad", lifespan = ""))

        assertNull(viewModel.averageLifespan.value)
        collectJob.cancel()
    }

    @Test
    fun `favorite toggle failure emits error event`() = runTest(testDispatcher) {
        doThrow(RuntimeException("DB error")).`when`(repository).toggleFavorite("abys")
        var errorReceived = false
        val collectJob = launch {
            viewModel.favoriteError.collect { errorReceived = true }
        }

        viewModel.onFavoriteToggle("abys")
        advanceUntilIdle()

        assertTrue(errorReceived)
        collectJob.cancel()
    }

    private fun TestScope.collectFavoriteBreeds(): Job {
        val job = backgroundScope.launch {
            viewModel.favoriteBreeds.collect {
                // Keeps the favorite breeds state flow active for emission assertions.
            }
        }
        advanceUntilIdle()
        return job
    }

    private fun TestScope.collectAverageLifespan(): Job {
        val job = backgroundScope.launch {
            viewModel.averageLifespan.collect {
                // Keeps the average lifespan state flow active for emission assertions.
            }
        }
        advanceUntilIdle()
        return job
    }

    private suspend fun TestScope.emitFavorites(vararg breeds: Breed) {
        favoriteBreeds.emit(breeds.toList())
        advanceUntilIdle()
    }

    private suspend fun TestScope.emitFavorites(breeds: List<Breed>) {
        favoriteBreeds.emit(breeds)
        advanceUntilIdle()
    }

    private fun breed(
        id: String,
        lifespan: String,
    ) = Breed(
        id = id,
        name = id,
        imageUrl = null,
        origin = "",
        temperament = "",
        description = "",
        lifespan = lifespan,
        isFavorite = true,
    )
}
