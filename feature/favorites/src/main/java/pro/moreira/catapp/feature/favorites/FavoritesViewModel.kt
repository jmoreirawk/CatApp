package pro.moreira.catapp.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pro.moreira.catapp.core.data.repository.BreedRepository
import pro.moreira.catapp.core.domain.model.Breed
import pro.moreira.catapp.core.domain.model.Lifespan
import pro.moreira.catapp.core.domain.model.averageSelectedValue
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: BreedRepository,
) : ViewModel() {
    val favoriteBreeds: StateFlow<List<Breed>> = repository.observeFavoriteBreeds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val averageLifespan: StateFlow<Double?> = favoriteBreeds
        .map { breeds ->
            breeds.mapNotNull { Lifespan.parse(it.lifespan) }
                .averageSelectedValue()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _favoriteError = Channel<Unit>(Channel.CONFLATED)
    val favoriteError: Flow<Unit> = _favoriteError.receiveAsFlow()

    fun onFavoriteToggle(breedId: String) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(breedId)
            } catch (_: Exception) {
                _favoriteError.trySend(Unit)
            }
        }
    }
}