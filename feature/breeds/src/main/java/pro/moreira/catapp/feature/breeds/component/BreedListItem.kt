package pro.moreira.catapp.feature.breeds.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import pro.moreira.catapp.core.domain.model.Breed
import pro.moreira.catapp.core.ui.theme.Dimens
import pro.moreira.catapp.feature.breeds.R

@Composable
fun BreedCard(
    breed: Breed,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
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
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (breed.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (breed.isFavorite) {
                        stringResource(R.string.remove_from_favourites)
                    } else {
                        stringResource(R.string.add_to_favourites)
                    },
                    tint = if (breed.isFavorite) {
                        Color.Red
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
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
            contentDescription = stringResource(
                R.string.breed_image_content_description,
                breedName
            ),
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
