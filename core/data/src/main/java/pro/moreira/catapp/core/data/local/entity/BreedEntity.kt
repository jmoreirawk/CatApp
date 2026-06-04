package pro.moreira.catapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breeds")
internal data class BreedEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val origin: String,
    val temperament: String,
    val description: String,
    val lifespan: String,
    val isFavorite: Boolean = false,
)
