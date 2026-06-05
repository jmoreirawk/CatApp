package pro.moreira.catapp.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pro.moreira.catapp.core.data.repository.BreedRepository
import pro.moreira.catapp.core.domain.model.Breed
import javax.inject.Inject

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Content(val breed: Breed) : DetailsUiState
    data class Error(val throwable: Throwable) : DetailsUiState
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: BreedRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _favoriteError = Channel<Unit>(Channel.CONFLATED)
    val favoriteError: Flow<Unit> = _favoriteError.receiveAsFlow()

    private var currentBreedId: String? = null
    private var isToggling = false

    fun loadBreed(breedId: String) {
        if (breedId == currentBreedId) return
        currentBreedId = breedId
        refresh()
    }

    fun retry() {
        refresh()
    }

    fun onFavoriteToggle() {
        val breedId = currentBreedId ?: return
        if (isToggling) return
        isToggling = true
        viewModelScope.launch {
            try {
                repository.toggleFavorite(breedId)
                val breed = repository.getBreed(breedId)
                _uiState.value = DetailsUiState.Content(breed)
            } catch (_: Exception) {
                _favoriteError.trySend(Unit)
            } finally {
                isToggling = false
            }
        }
    }

    private fun refresh() {
        val breedId = currentBreedId ?: return
        _uiState.value = DetailsUiState.Loading
        viewModelScope.launch {
            try {
                val breed = repository.getBreed(breedId)
                _uiState.value = DetailsUiState.Content(breed)
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e)
            }
        }
    }
}
