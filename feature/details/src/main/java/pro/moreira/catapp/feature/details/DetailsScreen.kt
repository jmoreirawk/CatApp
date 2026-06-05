package pro.moreira.catapp.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import pro.moreira.catapp.core.ui.component.CatImage
import pro.moreira.catapp.core.ui.component.FullScreenLoading
import pro.moreira.catapp.core.ui.component.MessageWithRetry
import pro.moreira.catapp.core.ui.component.userMessage
import pro.moreira.catapp.core.ui.theme.CatAppTheme
import pro.moreira.catapp.core.ui.theme.Dimens
import pro.moreira.catapp.feature.details.component.BreedDetailHeader
import pro.moreira.catapp.feature.details.component.DetailSection
import pro.moreira.catapp.feature.details.component.SectionDivider

@Composable
fun DetailsScreen(
    breedId: String,
    onBackClick: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteErrorMessage = stringResource(R.string.details_favorite_error)

    LaunchedEffect(breedId) {
        viewModel.loadBreed(breedId)
    }

    LaunchedEffect(Unit) {
        viewModel.favoriteError.collect {
            snackbarHostState.showSnackbar(message = favoriteErrorMessage)
        }
    }

    DetailsScaffold(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsScaffold(
    uiState: DetailsUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onFavoriteToggle: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            val title = when (uiState) {
                is DetailsUiState.Content -> uiState.breed.name
                else -> stringResource(R.string.details_title)
            }
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.details_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is DetailsUiState.Loading -> FullScreenLoading(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )

            is DetailsUiState.Error -> {
                MessageWithRetry(
                    title = stringResource(R.string.details_error_message),
                    message = uiState.throwable.userMessage(
                        fallback = stringResource(R.string.details_error_message),
                    ),
                    retryText = stringResource(R.string.details_retry),
                    onRetry = onRetry,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
            }

            is DetailsUiState.Content -> DetailsContent(
                breed = uiState.breed,
                onFavoriteToggle = onFavoriteToggle,
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DetailsContent(
    breed: Breed,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        CatImage(
            imageUrl = breed.imageUrl,
            contentDescription = stringResource(R.string.breed_image_content_description, breed.name),
            fallbackText = breed.name.take(1).uppercase(),
            fallbackTextStyle = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        )

        Column(
            modifier = Modifier.padding(
                start = Dimens.spacingLarge,
                end = Dimens.spacingLarge,
                top = Dimens.spacingLarge,
                bottom = Dimens.spacingExtraLarge + Dimens.spacingLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            BreedDetailHeader(
                name = breed.name,
                isFavorite = breed.isFavorite,
                onFavoriteToggle = onFavoriteToggle,
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            SectionDivider()
            DetailSection(
                label = stringResource(R.string.details_origin_label),
                value = breed.origin,
            )

            SectionDivider()
            DetailSection(
                label = stringResource(R.string.details_temperament_label),
                value = breed.temperament,
            )

            SectionDivider()
            DetailSection(
                label = stringResource(R.string.details_description_label),
                value = breed.description,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScaffoldPreview() {
    CatAppTheme {
        DetailsScaffold(
            uiState = DetailsUiState.Content(
                Breed(
                    id = "abys",
                    name = "Abyssinian",
                    imageUrl = null,
                    origin = "Egypt",
                    temperament = "Active, Energetic, Independent, Intelligent, Gentle",
                    description = "The Abyssinian is a slender, fine-boned, muscular, medium-sized cat. The coat is soft, silky, and fine in texture. The coat is not a solid color, but is actually ticked with alternating bands of light and dark colors. The Abyssinian is known for its playful and active nature. They are often described as intelligent, curious, and affectionate cats that enjoy interactive play and climbing.",
                    lifespan = "14 - 15",
                    isFavorite = true,
                ),
            ),
            onBackClick = {},
            onRetry = {},
            onFavoriteToggle = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
