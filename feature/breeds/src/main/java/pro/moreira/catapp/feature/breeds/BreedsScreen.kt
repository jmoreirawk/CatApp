package pro.moreira.catapp.feature.breeds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import pro.moreira.catapp.core.domain.model.Breed
import pro.moreira.catapp.core.ui.theme.CatAppTheme
import pro.moreira.catapp.core.ui.theme.Dimens

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
        when {
            breeds.loadState.refresh is LoadState.Loading -> {
                FullScreenLoading(modifier = Modifier.padding(innerPadding))
            }

            breeds.loadState.refresh is LoadState.Error && breeds.itemCount == 0 -> {
                FullScreenError(
                    message = (breeds.loadState.refresh as LoadState.Error).error.userMessage(
                        fallback = stringResource(R.string.breeds_error_message),
                    ),
                    onRetry = breeds::retry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            breeds.itemCount == 0 -> {
                EmptyBreeds(modifier = Modifier.padding(innerPadding))
            }

            else -> {
                BreedList(
                    breeds = breeds,
                    onBreedClick = onBreedClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
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

        when (val append = breeds.loadState.append) {
            is LoadState.Loading -> item { AppendLoading() }
            is LoadState.Error -> item {
                AppendError(
                    message = append.error.userMessage(
                        fallback = stringResource(R.string.breeds_error_message),
                    ),
                    onRetry = breeds::retry,
                )
            }
            else -> Unit
        }
    }
}

@Composable
private fun BreedCard(
    breed: Breed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
        ) {
            BreedImage(
                imageUrl = breed.imageUrl,
                breedName = breed.name,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = breed.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (breed.origin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.spacingExtraSmall))
                    Text(
                        text = breed.origin,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BreedImage(
    imageUrl: String?,
    breedName: String,
    modifier: Modifier = Modifier,
) {
    var failed by remember(imageUrl) { mutableStateOf(imageUrl.isNullOrBlank()) }
    val imageModifier = modifier
        .size(Dimens.imageSizeLarge)
        .clip(RoundedCornerShape(Dimens.cornerRadiusMedium))
        .background(MaterialTheme.colorScheme.secondaryContainer)

    if (failed) {
        ImageFallback(
            breedName = breedName,
            modifier = imageModifier,
        )
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.breed_image_content_description, breedName),
            contentScale = ContentScale.Crop,
            onError = { failed = true },
            modifier = imageModifier,
        )
    }
}

@Composable
private fun ImageFallback(
    breedName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = breedName.take(1).uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageWithRetry(
        title = stringResource(R.string.breeds_error_title),
        message = message,
        onRetry = onRetry,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun EmptyBreeds(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.breeds_empty),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AppendLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacingLarge),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(Dimens.progressIndicatorSize))
    }
}

@Composable
private fun AppendError(
    message: String,
    onRetry: () -> Unit,
) {
    MessageWithRetry(
        title = stringResource(R.string.breeds_append_error_title),
        message = message,
        onRetry = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacingLarge),
    )
}

@Composable
private fun MessageWithRetry(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Dimens.spacingExtraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(Dimens.spacingSmall))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

private fun Throwable.userMessage(fallback: String): String = localizedMessage
    ?.takeIf(String::isNotBlank)
    ?: fallback

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
