package pro.moreira.catapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import pro.moreira.catapp.feature.breeds.BreedsScreen
import pro.moreira.catapp.feature.details.DetailsScreen
import pro.moreira.catapp.feature.favorites.FavoritesScreen

@Serializable
private data object Breeds : NavKey

@Serializable
private data class Details(val breedId: String) : NavKey

@Serializable
private data object Favorites : NavKey

@Composable
fun CatAppNavHost() {
    val backStack = rememberNavBackStack(Breeds)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {
            entry<Breeds> {
                BreedsScreen(
                    onBreedClick = { breedId ->
                        // Logic to prevent double clicks
                        val destination = Details(breedId)
                        if (backStack.lastOrNull() != destination) {
                            backStack.add(destination)
                        }
                    },
                )
            }
            entry<Details> { destination ->
                DetailsScreen(breedId = destination.breedId)
            }
            entry<Favorites> {
                FavoritesScreen()
            }
        },
    )
}
