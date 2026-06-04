package pro.moreira.catapp.feature.details

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DetailsScreen(breedId: String) {
    Text(text = "Details: $breedId")
}
