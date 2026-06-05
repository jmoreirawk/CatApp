package pro.moreira.catapp.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pro.moreira.catapp.core.domain.model.Breed
import pro.moreira.catapp.core.ui.component.EmptyState
import pro.moreira.catapp.core.ui.theme.CatAppTheme
import pro.moreira.catapp.core.ui.theme.Dimens
import pro.moreira.catapp.feature.favorites.component.FavoriteBreedCard
import pro.moreira.catapp.feature.favorites.component.LifespanSummaryCard

@Composable
fun FavoritesScreen(
    onBreedClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val breeds by viewModel.favoriteBreeds.collectAsStateWithLifecycle()
    val averageLifespan by viewModel.averageLifespan.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.favorites_favorite_error)

    LaunchedEffect(Unit) {
        viewModel.favoriteError.collect {
            snackbarHostState.showSnackbar(message = errorMessage)
        }
    }

    FavoritesScreenContent(
        breeds = breeds,
        averageLifespan = averageLifespan,
        onBreedClick = onBreedClick,
        onBackClick = onBackClick,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    breeds: List<Breed>,
    averageLifespan: Double?,
    onBreedClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.favorites_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        if (breeds.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.favorites_empty),
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(Dimens.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            ) {
                item(key = "lifespan_summary") {
                    LifespanSummaryCard(
                        averageLifespan = averageLifespan,
                    )
                }
                items(
                    items = breeds,
                    key = { it.id },
                ) { breed ->
                    FavoriteBreedCard(
                        breed = breed,
                        onClick = { onBreedClick(breed.id) },
                        onFavoriteToggle = { onFavoriteToggle(breed.id) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    CatAppTheme {
        FavoritesScreenContent(
            breeds = listOf(
                Breed(
                    id = "abys",
                    name = "Abyssinian",
                    imageUrl = null,
                    origin = "Egypt",
                    temperament = "",
                    description = "",
                    lifespan = "14 - 15",
                    isFavorite = true,
                ),
                Breed(
                    id = "aege",
                    name = "Aegean",
                    imageUrl = null,
                    origin = "Greece",
                    temperament = "",
                    description = "",
                    lifespan = "9 - 12",
                    isFavorite = true,
                ),
            ),
            averageLifespan = 11.5,
            onBreedClick = {},
            onBackClick = {},
            onFavoriteToggle = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenEmptyPreview() {
    CatAppTheme {
        FavoritesScreenContent(
            breeds = emptyList(),
            averageLifespan = null,
            onBreedClick = {},
            onBackClick = {},
            onFavoriteToggle = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}