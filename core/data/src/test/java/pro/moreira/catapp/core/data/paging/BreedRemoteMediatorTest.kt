package pro.moreira.catapp.core.data.paging

import org.junit.Assert.assertEquals
import org.junit.Test

class BreedRemoteMediatorTest {
    @Test
    fun `fresh cache skips initial refresh`() {
        val now = 10_000L
        val freshness = 1_000L

        assertEquals(
            true,
            BreedRemoteMediator.isCacheFresh(
                lastUpdatedAtMillis = now - freshness + 1,
                nowMillis = now,
                freshnessMillis = freshness,
            ),
        )
    }

    @Test
    fun `stale cache launches initial refresh`() {
        val now = 10_000L
        val freshness = 1_000L

        assertEquals(
            false,
            BreedRemoteMediator.isCacheFresh(
                lastUpdatedAtMillis = now - freshness,
                nowMillis = now,
                freshnessMillis = freshness,
            ),
        )
    }
}
