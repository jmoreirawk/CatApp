package pro.moreira.catapp.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BreedDto(
    val id: String,
    val name: String,
    val image: ImageDto? = null,
    val origin: String = "",
    val temperament: String = "",
    val description: String = "",
    @SerialName("life_span")
    val lifespan: String = "",
)

@Serializable
internal data class ImageDto(
    val url: String? = null,
)
