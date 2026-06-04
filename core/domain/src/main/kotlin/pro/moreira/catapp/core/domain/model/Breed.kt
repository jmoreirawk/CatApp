package pro.moreira.catapp.core.domain.model

data class Breed(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val origin: String,
    val temperament: String,
    val description: String,
    val lifespan: String,
    val isFavorite: Boolean = false,
)
