package pro.moreira.catapp.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreedDto(
    val id: String,
    val name: String,
    val image: ImageDto? = null,
    val origin: String = "",
    val temperament: String = "",
    val description: String = "",
    @SerialName("reference_image_id")
    val referenceImageId: String? = null,
    @SerialName("life_span")
    val lifespan: String = "",
)

@Serializable
data class ImageDto(
    val id: String? = null,
    val url: String? = null,
)
