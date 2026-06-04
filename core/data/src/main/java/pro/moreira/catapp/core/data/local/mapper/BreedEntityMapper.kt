package pro.moreira.catapp.core.data.local.mapper

import pro.moreira.catapp.core.data.local.entity.BreedEntity
import pro.moreira.catapp.core.domain.model.Breed

internal fun BreedEntity.toDomain() = Breed(
    id = id,
    name = name,
    imageUrl = imageUrl,
    origin = origin,
    temperament = temperament,
    description = description,
    lifespan = lifespan,
    isFavorite = isFavorite,
)
