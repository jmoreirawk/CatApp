package pro.moreira.catapp.feature.breeds

import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doThrow
import pro.moreira.catapp.core.data.repository.BreedRepository
import pro.moreira.catapp.core.domain.model.Breed
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BreedsViewModelTest {
    private val repository = mock<BreedRepository>()
    private val breedQueries = mutableListOf<String>()
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BreedsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        stubBreedQueries()
        viewModel = BreedsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onQueryChange updates query state`() {
        viewModel.onQueryChange("bengal")

        assertEquals("bengal", viewModel.query.value)
    }

    @Test
    fun `search waits for debounce before querying repository`() = runTest(testDispatcher) {
        val collectJob = collectBreeds()

        typeQuery("bengal")
        advanceTimeBy(299.milliseconds)
        assertFalse(breedQueries.contains("bengal"))

        advanceTimeBy(1.milliseconds)
        advanceUntilIdle()
        assertTrue(breedQueries.contains("bengal"))
        collectJob.cancel()
    }

    @Test
    fun `search only queries latest value after rapid changes`() = runTest(testDispatcher) {
        val collectJob = collectBreeds()

        typeQuery("b")
        advanceTimeBy(100.milliseconds)
        typeQuery("be")
        advanceTimeBy(100.milliseconds)
        typeQuery("ben")
        advanceTimeBy(300.milliseconds)
        advanceUntilIdle()

        assertFalse(breedQueries.contains("b"))
        assertFalse(breedQueries.contains("be"))
        assertTrue(breedQueries.contains("ben"))
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

    private fun TestScope.collectBreeds(): Job {
        val job = backgroundScope.launch {
            viewModel.breeds.collect {
                // Keeps the paging flow active for debounce testing.
            }
        }
        advanceUntilIdle()
        return job
    }

    private fun typeQuery(query: String) {
        viewModel.onQueryChange(query)
    }

    private fun emptyBreedPagingData() = flowOf(PagingData.empty<Breed>())

    @Suppress("UnusedFlow")
    private fun stubBreedQueries() {
        doAnswer { invocation ->
            breedQueries += invocation.getArgument<String>(0)
            emptyBreedPagingData()
        }.`when`(repository).getBreeds(anyString())
    }
}
