package pro.moreira.catapp.core.data.remote.mapper

import pro.moreira.catapp.core.data.remote.dto.BreedDto
import pro.moreira.catapp.core.data.local.entity.BreedEntity
import pro.moreira.catapp.core.domain.model.Breed

internal fun BreedDto.toDomain() = Breed(
    id = id,
    name = name,
    imageUrl = image?.url,
    origin = origin,
    temperament = temperament,
    description = description,
    lifespan = lifespan,
)

internal fun BreedDto.toEntity() = BreedEntity(
    id = id,
    name = name,
    imageUrl = image?.url,
    origin = origin,
    temperament = temperament,
    description = description,
    lifespan = lifespan,
)
