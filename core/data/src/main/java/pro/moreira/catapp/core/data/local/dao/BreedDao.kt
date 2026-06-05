package pro.moreira.catapp.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pro.moreira.catapp.core.data.local.entity.BreedEntity

@Dao
internal interface BreedDao {
    @Query(
        """
        SELECT breeds.*
        FROM breeds
        INNER JOIN breed_remote_keys ON breeds.id = breed_remote_keys.breedId
        WHERE breed_remote_keys.query = :query
        ORDER BY breed_remote_keys.position ASC
        """,
    )
    fun pagingSource(query: String): PagingSource<Int, BreedEntity>

    @Query(
        """
        SELECT *
        FROM breeds
        WHERE name LIKE '%' || :name || '%'
        ORDER BY name ASC
        """,
    )
    fun searchPagingSource(name: String): PagingSource<Int, BreedEntity>

    @Query("SELECT * FROM breeds WHERE id = :id")
    suspend fun getBreed(id: String): BreedEntity?

    @Query("SELECT * FROM breeds WHERE isFavorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<BreedEntity>>

    @Query("UPDATE breeds SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String): Int

    @Query(
        """
        INSERT INTO breeds (
            id,
            name,
            imageUrl,
            origin,
            temperament,
            description,
            lifespan,
            isFavorite
        ) VALUES (
            :id,
            :name,
            :imageUrl,
            :origin,
            :temperament,
            :description,
            :lifespan,
            0
        )
        ON CONFLICT(id) DO UPDATE SET
            name = excluded.name,
            imageUrl = excluded.imageUrl,
            origin = excluded.origin,
            temperament = excluded.temperament,
            description = excluded.description,
            lifespan = excluded.lifespan
        """,
    )
    suspend fun upsertRemoteFieldsPreservingFavorite(
        id: String,
        name: String,
        imageUrl: String?,
        origin: String,
        temperament: String,
        description: String,
        lifespan: String,
    )
}
