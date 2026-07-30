package pro.moreira.catapp.core.data.remote.mapper

import pro.moreira.catapp.core.data.remote.dto.BreedDto
import pro.moreira.catapp.core.data.local.entity.BreedEntity
import pro.moreira.catapp.core.domain.model.Breed

internal fun BreedDto.toDomain() = Breed(
    id = id,
    name = name,
    imageUrl = imageUrl(),
    origin = origin,
    temperament = temperament,
    description = description,
    lifespan = lifespan,
)

internal fun BreedDto.toEntity() = BreedEntity(
    id = id,
    name = name,
    imageUrl = imageUrl(),
    origin = origin,
    temperament = temperament,
    description = description,
    lifespan = lifespan,
)

private fun BreedDto.imageUrl(): String? =
    image?.url?.takeIf { it.isNotBlank() }
        ?: referenceImageId?.takeIf { it.isNotBlank() }?.toCatImageUrl()
        ?: image?.id?.takeIf { it.isNotBlank() }?.toCatImageUrl()

private fun String.toCatImageUrl(): String =
    if (contains('.')) {
        "$CAT_IMAGE_BASE_URL$this"
    } else {
        "$CAT_IMAGE_BASE_URL$this.jpg"
    }

private const val CAT_IMAGE_BASE_URL = "https://cdn2.thecatapi.com/images/"
