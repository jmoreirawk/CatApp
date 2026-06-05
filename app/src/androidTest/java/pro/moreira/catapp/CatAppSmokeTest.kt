package pro.moreira.catapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pro.moreira.catapp.core.data.di.NetworkModule
import pro.moreira.catapp.core.data.remote.CatApiService
import pro.moreira.catapp.core.data.remote.dto.BreedDto
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class CatAppSmokeTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var apiService: CatApiService

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .deleteDatabase("cat-app.db")
        hiltRule.inject()
        check(apiService === SmokeCatApiService) {
            "Smoke test must use SmokeCatApiService instead of the live Cat API."
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun favoriteBreedAppearsInFavoritesWithAggregateLifespan() {
        waitForText(TEST_BREED_NAME)
        composeRule.onNodeWithText(TEST_BREED_NAME).assertIsDisplayed()

        composeRule.onNodeWithTag("breed_card_$TEST_BREED_ID").performClick()
        waitForText(TEST_BREED_ORIGIN)
        composeRule.onNodeWithText(TEST_BREED_DESCRIPTION).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Add to favorites").performClick()
        waitForContentDescription("Remove from favorites")
        composeRule.onNodeWithContentDescription("Back").performClick()

        waitForText(TEST_BREED_NAME)
        composeRule.onNodeWithContentDescription("Favorites").performClick()

        waitForText("Average Lifespan")
        composeRule.onNodeWithText(TEST_BREED_NAME).assertIsDisplayed()
        composeRule.onNodeWithText("14.0 years").assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForContentDescription(contentDescription: String) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithContentDescription(contentDescription)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private companion object {
        const val TEST_BREED_ID = "smoke-abys"
        const val TEST_BREED_NAME = "Smoke Abyssinian"
        const val TEST_BREED_ORIGIN = "Egypt"
        const val TEST_BREED_DESCRIPTION = "A deterministic breed used by the end-to-end smoke test."
        const val TIMEOUT_MILLIS = 5_000L
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SmokeNetworkModule {
    @Provides
    @Singleton
    fun provideCatApiService(): CatApiService = SmokeCatApiService
}

private object SmokeCatApiService : CatApiService {
    private val breeds = listOf(
        BreedDto(
            id = "smoke-abys",
            name = "Smoke Abyssinian",
            origin = "Egypt",
            temperament = "Active, Energetic",
            description = "A deterministic breed used by the end-to-end smoke test.",
            lifespan = "14 - 15",
        ),
    )

    override suspend fun getBreeds(
        page: Int,
        limit: Int,
        order: String,
    ): List<BreedDto> = if (page == 0) breeds else emptyList()

    override suspend fun getBreed(breedId: String): BreedDto =
        breeds.first { it.id == breedId }
}
