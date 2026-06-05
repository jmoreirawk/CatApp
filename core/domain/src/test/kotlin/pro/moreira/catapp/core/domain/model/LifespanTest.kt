package pro.moreira.catapp.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LifespanTest {

    @Test
    fun `parses lower and upper from range`() {
        val lifespan = Lifespan.parse("14 - 15")
        assertEquals(14, lifespan?.lower)
        assertEquals(15, lifespan?.upper)
    }

    @Test
    fun `parses single value as both lower and upper`() {
        val lifespan = Lifespan.parse("12")
        assertEquals(12, lifespan?.lower)
        assertEquals(12, lifespan?.upper)
    }

    @Test
    fun `blank string returns null`() {
        assertNull(Lifespan.parse(""))
        assertNull(Lifespan.parse("   "))
    }

    @Test
    fun `malformed string returns null`() {
        assertNull(Lifespan.parse("abc"))
        assertNull(Lifespan.parse("-"))
        assertNull(Lifespan.parse("10 -"))
        assertNull(Lifespan.parse("- 5"))
    }

    @Test
    fun `selected value is lower bound`() {
        val lifespan = Lifespan.parse("14 - 15")
        assertEquals(14, lifespan?.selectedValue)
    }

    @Test
    fun `average uses selected lower values`() {
        val lifespans = listOfNotNull(
            Lifespan.parse("14 - 15"),
            Lifespan.parse("9 - 12"),
        )
        val average = lifespans.averageSelectedValue()
        assertEquals(11.5, average!!, 0.001)
    }

    @Test
    fun `average of empty list returns null`() {
        assertNull(emptyList<Lifespan>().averageSelectedValue())
    }
}