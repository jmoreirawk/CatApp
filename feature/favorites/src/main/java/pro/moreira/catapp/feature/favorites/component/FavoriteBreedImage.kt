package pro.moreira.catapp.feature.favorites.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import pro.moreira.catapp.core.ui.theme.Dimens
import pro.moreira.catapp.feature.favorites.R

@Composable
fun FavoriteBreedImage(
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
        Box(
            modifier = imageModifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = breedName.take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(
                R.string.breed_image_content_description,
                breedName,
            ),
            contentScale = ContentScale.Crop,
            onError = { failed = true },
            modifier = imageModifier,
        )
    }
}