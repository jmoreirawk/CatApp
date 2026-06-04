package pro.moreira.catapp.feature.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import pro.moreira.catapp.core.data.repository.BreedRepository
import javax.inject.Inject

@HiltViewModel
class BreedsViewModel @Inject constructor(
    repository: BreedRepository,
) : ViewModel() {
    val breeds = repository.getBreeds().cachedIn(viewModelScope)
}
