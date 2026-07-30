package pro.moreira.catapp.core.data.remote.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pro.moreira.catapp.core.data.remote.dto.BreedDto
import pro.moreira.catapp.core.data.remote.dto.ImageDto
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

    @Test
    fun `dto maps to cache entity without favorite state`() {
        val entity =
            BreedDto(
                id = "abys",
                name = "Abyssinian",
                image = ImageDto(url = "https://example.com/cat.jpg"),
                origin = "Egypt",
                temperament = "Active",
                description = "Ancient breed",
                lifespan = "14 - 15",
            ).toEntity()

        assertEquals("abys", entity.id)
        assertEquals("Abyssinian", entity.name)
        assertEquals("https://example.com/cat.jpg", entity.imageUrl)
        assertEquals("Egypt", entity.origin)
        assertEquals("Active", entity.temperament)
        assertEquals("Ancient breed", entity.description)
        assertEquals("14 - 15", entity.lifespan)
        assertEquals(false, entity.isFavorite)
    }

    @Test
    fun `reference image id maps to cat image url when image url is missing`() {
        val breed = BreedDto(
            id = "abys",
            name = "Abyssinian",
            referenceImageId = "0XYvRd7oD",
        ).toDomain()

        assertEquals("https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg", breed.imageUrl)
    }

    @Test
    fun `image id maps to cat image url when image url is missing`() {
        val breed = BreedDto(
            id = "abys",
            name = "Abyssinian",
            image = ImageDto(id = "0XYvRd7oD"),
        ).toDomain()

        assertEquals("https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg", breed.imageUrl)
    }
}
