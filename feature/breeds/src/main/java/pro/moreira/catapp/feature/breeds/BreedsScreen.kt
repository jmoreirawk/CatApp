package pro.moreira.catapp.feature.breeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.MutableStateFlow
import pro.moreira.catapp.core.domain.model.Breed
import pro.moreira.catapp.core.ui.component.EmptyState
import pro.moreira.catapp.core.ui.component.MessageWithRetry
import pro.moreira.catapp.core.ui.component.PagingContent
import pro.moreira.catapp.core.ui.component.pagingAppendState
import pro.moreira.catapp.core.ui.component.userMessage
import pro.moreira.catapp.core.ui.theme.CatAppTheme
import pro.moreira.catapp.core.ui.theme.Dimens
import pro.moreira.catapp.feature.breeds.component.BreedCard

@Composable
fun BreedsScreen(
    onBreedClick: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: BreedsViewModel = hiltViewModel(),
) {
    val breeds = viewModel.breeds.collectAsLazyPagingItems()
    val query by viewModel.query.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.favorite_error_message)

    LaunchedEffect(Unit) {
        viewModel.favoriteError.collect {
            snackbarHostState.showSnackbar(message = errorMessage)
        }
    }

    BreedsScreenContent(
        breeds = breeds,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onBreedClick = onBreedClick,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onNavigateToFavorites = onNavigateToFavorites,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BreedsScreenContent(
    breeds: LazyPagingItems<Breed>,
    query: String,
    onQueryChange: (String) -> Unit,
    onBreedClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.breeds_title)) },
                actions = {
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.favorites_navigate),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingLarge, vertical = Dimens.spacingMedium),
            )
            PagingContent(
                items = breeds,
                modifier = Modifier.fillMaxSize(),
                empty = { modifier ->
                    val message = if (query.isNotBlank()) {
                        stringResource(R.string.breeds_search_empty, query)
                    } else {
                        stringResource(R.string.breeds_empty)
                    }
                    EmptyState(message = message, modifier = modifier)
                },
                error = { throwable, retry, modifier ->
                    MessageWithRetry(
                        title = stringResource(R.string.breeds_error_title),
                        message = throwable.userMessage(
                            fallback = stringResource(R.string.breeds_error_message),
                        ),
                        retryText = stringResource(R.string.retry),
                        onRetry = retry,
                        modifier = modifier,
                    )
                },
            ) { items, modifier ->
                BreedList(
                    breeds = items,
                    onBreedClick = onBreedClick,
                    onFavoriteToggle = onFavoriteToggle,
                    modifier = modifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.breeds_search_hint)) },
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(onClick = { onQueryChange("") }) {
                    Text(stringResource(R.string.breeds_search_clear))
                }
            }
        },
    )
}

@Composable
private fun BreedList(
    breeds: LazyPagingItems<Breed>,
    onBreedClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
    ) {
        items(
            count = breeds.itemCount,
            key = breeds.itemKey { it.id },
        ) { index ->
            breeds[index]?.let { breed ->
                BreedCard(
                    breed = breed,
                    onClick = { onBreedClick(breed.id) },
                    onFavoriteToggle = { onFavoriteToggle(breed.id) },
                )
            }
        }
        pagingAppendState(
            items = breeds,
            error = { throwable, retry ->
                MessageWithRetry(
                    title = stringResource(R.string.breeds_append_error_title),
                    message = throwable.userMessage(
                        fallback = stringResource(R.string.breeds_error_message),
                    ),
                    retryText = stringResource(R.string.retry),
                    onRetry = retry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingLarge),
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BreedsScreenPreview() {
    val previewBreeds = MutableStateFlow(
        PagingData.from(
            listOf(
                Breed(
                    id = "abys",
                    name = "Abyssinian",
                    imageUrl = null,
                    origin = "Egypt",
                    temperament = "",
                    description = "",
                    lifespan = "14 - 15",
                ),
                Breed(
                    id = "aege",
                    name = "Aegean",
                    imageUrl = null,
                    origin = "Greece",
                    temperament = "",
                    description = "",
                    lifespan = "9 - 12",
                ),
            ),
        ),
    ).collectAsLazyPagingItems()

    CatAppTheme {
        BreedsScreenContent(
            breeds = previewBreeds,
            query = "",
            onQueryChange = {},
            onBreedClick = {},
            onFavoriteToggle = {},
            onNavigateToFavorites = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
