package pro.moreira.catapp.feature.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pro.moreira.catapp.core.data.repository.BreedRepository
import pro.moreira.catapp.core.domain.model.Breed
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BreedsViewModel @Inject constructor(
    private val repository: BreedRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val breeds: Flow<PagingData<Breed>> = query
        .debounce(SEARCH_DEBOUNCE_MILLIS.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { repository.getBreeds(it) }
        .cachedIn(viewModelScope)

    private val _favoriteError = Channel<Unit>(Channel.CONFLATED)
    val favoriteError: Flow<Unit> = _favoriteError.receiveAsFlow()

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onFavoriteToggle(breedId: String) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(breedId)
            } catch (_: Exception) {
                _favoriteError.trySend(Unit)
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
