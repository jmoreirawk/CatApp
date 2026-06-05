package pro.moreira.catapp.feature.details

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import pro.moreira.catapp.core.data.repository.BreedRepository
import pro.moreira.catapp.core.domain.model.Breed

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    private val repository = mock(BreedRepository::class.java)
    private lateinit var viewModel: DetailsViewModel
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DetailsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBreed emits Content with correct breed`() = runTest(testDispatcher) {
        val breed = breed()
        doReturn(breed).`when`(repository).getBreed("abys")

        viewModel.loadBreed("abys")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Content state", state is DetailsUiState.Content)
        val contentState = state as DetailsUiState.Content
        assertEquals(breed, contentState.breed)
    }

    @Test
    fun `retry after error recovers to Content`() = runTest(testDispatcher) {
        doThrow(RuntimeException("Network error")).`when`(repository).getBreed("abys")

        viewModel.loadBreed("abys")
        advanceUntilIdle()
        assertTrue("Expected Error state", viewModel.uiState.value is DetailsUiState.Error)

        val breed = breed()
        doReturn(breed).`when`(repository).getBreed("abys")

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Content state after retry", state is DetailsUiState.Content)
        val contentState = state as DetailsUiState.Content
        assertEquals(breed, contentState.breed)
    }

    @Test
    fun `favorite toggle failure preserves content`() = runTest(testDispatcher) {
        val breed = breed()
        doReturn(breed).`when`(repository).getBreed("abys")
        viewModel.loadBreed("abys")
        advanceUntilIdle()
        assertTrue("Expected Content state", viewModel.uiState.value is DetailsUiState.Content)

        doThrow(RuntimeException("DB error")).`when`(repository).toggleFavorite("abys")

        viewModel.onFavoriteToggle()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Content state after toggle failure", state is DetailsUiState.Content)
        val contentState = state as DetailsUiState.Content
        assertEquals(breed, contentState.breed)
    }

    @Test
    fun `favorite toggle failure emits error event`() = runTest(testDispatcher) {
        val breed = breed()
        doReturn(breed).`when`(repository).getBreed("abys")
        viewModel.loadBreed("abys")
        advanceUntilIdle()

        doThrow(RuntimeException("DB error")).`when`(repository).toggleFavorite("abys")

        var errorReceived = false
        val collectJob = launch {
            viewModel.favoriteError.collect { errorReceived = true }
        }

        viewModel.onFavoriteToggle()
        advanceUntilIdle()

        assertTrue("Expected error event on favorite toggle failure", errorReceived)
        collectJob.cancel()
    }

    private fun breed() = Breed(
        id = "abys",
        name = "Abyssinian",
        imageUrl = null,
        origin = "Egypt",
        temperament = "Active",
        description = "A breed description.",
        lifespan = "14 - 15",
    )
}
