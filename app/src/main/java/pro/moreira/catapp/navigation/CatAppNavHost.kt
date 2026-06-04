package pro.moreira.catapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pro.moreira.catapp.feature.breeds.BreedsScreen
import pro.moreira.catapp.feature.details.DetailsScreen
import pro.moreira.catapp.feature.favorites.FavoritesScreen

private const val BREEDS_ROUTE = "breeds"
private const val DETAILS_ROUTE = "details"
private const val FAVORITES_ROUTE = "favorites"

@Composable
fun CatAppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BREEDS_ROUTE,
    ) {
        composable(BREEDS_ROUTE) {
            BreedsScreen()
        }
        composable(DETAILS_ROUTE) {
            DetailsScreen()
        }
        composable(FAVORITES_ROUTE) {
            FavoritesScreen()
        }
    }
}
