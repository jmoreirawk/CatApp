package pro.moreira.catapp.core.data.remote

import pro.moreira.catapp.core.data.remote.dto.BreedDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatApiService {
    @GET("v1/breeds")
    suspend fun getBreeds(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("order") order: String = "ASC",
    ): List<BreedDto>

    @GET("v1/breeds/{breedId}")
    suspend fun getBreed(@Path("breedId") breedId: String): BreedDto
}
