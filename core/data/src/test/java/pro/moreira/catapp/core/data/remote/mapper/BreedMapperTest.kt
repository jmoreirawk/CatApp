package pro.moreira.catapp.core.data.remote.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pro.moreira.catapp.core.data.remote.dto.BreedDto
import pro.moreira.catapp.core.domain.model.Breed

class BreedMapperTest {
    @Test
    fun `missing optional fields map to safe domain values`() {
        val breed = BreedDto(id = "abys", name = "Abyssinian").toDomain()

        assertEquals(
            Breed(
                id = "abys",
                name = "Abyssinian",
                imageUrl = null,
                origin = "",
                temperament = "",
                description = "",
                lifespan = "",
            ),
            breed,
        )
        assertNull(breed.imageUrl)
    }
}
