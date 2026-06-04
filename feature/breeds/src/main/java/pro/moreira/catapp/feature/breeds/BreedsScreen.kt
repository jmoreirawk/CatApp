package pro.moreira.catapp.feature.breeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    viewModel: BreedsViewModel = hiltViewModel(),
) {
    val breeds = viewModel.breeds.collectAsLazyPagingItems()
    BreedsScreenContent(
        breeds = breeds,
        onBreedClick = onBreedClick,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BreedsScreenContent(
    breeds: LazyPagingItems<Breed>,
    onBreedClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.breeds_title)) })
        },
    ) { innerPadding ->
        PagingContent(
            items = breeds,
            modifier = Modifier.padding(innerPadding),
            empty = {
                EmptyState(message = stringResource(R.string.breeds_empty))
            },
            error = { throwable, retry ->
                MessageWithRetry(
                    title = stringResource(R.string.breeds_error_title),
                    message = throwable.userMessage(
                        fallback = stringResource(R.string.breeds_error_message),
                    ),
                    retryText = stringResource(R.string.retry),
                    onRetry = retry,
                )
            },
        ) { items ->
            BreedList(
                breeds = items,
                onBreedClick = onBreedClick,
            )
        }
    }
}

@Composable
private fun BreedList(
    breeds: LazyPagingItems<Breed>,
    onBreedClick: (String) -> Unit,
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
            onBreedClick = {},
        )
    }
}
