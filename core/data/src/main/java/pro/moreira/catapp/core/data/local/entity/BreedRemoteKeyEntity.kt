package pro.moreira.catapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "breed_remote_keys",
    primaryKeys = ["query", "breedId"],
    foreignKeys = [
        ForeignKey(
            entity = BreedEntity::class,
            parentColumns = ["id"],
            childColumns = ["breedId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("breedId"),
        Index(value = ["query", "position"]),
    ],
)
internal data class BreedRemoteKeyEntity(
    val query: String,
    val breedId: String,
    val nextPage: Int?,
    val position: Int,
)
