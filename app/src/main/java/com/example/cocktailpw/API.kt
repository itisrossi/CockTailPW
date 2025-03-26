package com.example.cocktailpw

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApiResponse(
    @SerializedName("drinks")
    val drinks: List<Drink>?
)


data class Drink(
    @SerializedName("idDrink")
    val idDrink: String,
    @SerializedName("strDrink")
    val strDrink: String,
    @SerializedName("strDrinkThumb")
    val strDrinkThumb: String,
    @SerializedName("strInstructions")
    val strInstructions: String?,
    @SerializedName("strCategory")
    val strCategory: String?,
    @SerializedName("strAlcoholic")
    val strAlcoholic: String?,
    @SerializedName("strGlass")
    val strGlass: String?,
    @SerializedName("strIngredient1")
    val strIngredient1: String?,
    @SerializedName("strIngredient2")
    val strIngredient2: String?,
    @SerializedName("strIngredient3")
    val strIngredient3: String?,
    @SerializedName("strIngredient4")
    val strIngredient4: String?,
    @SerializedName("strIngredient5")
    val strIngredient5: String?,
    @SerializedName("strIngredient6")
    val strIngredient6: String?,
    @SerializedName("strIngredient7")
    val strIngredient7: String?,
    @SerializedName("strIngredient8")
    val strIngredient8: String?,
    @SerializedName("strIngredient9")
    val strIngredient9: String?,
    @SerializedName("strIngredient10")
    val strIngredient10: String?,
    @SerializedName("strIngredient11")
    val strIngredient11: String?,
    @SerializedName("strIngredient12")
    val strIngredient12: String?,
    @SerializedName("strIngredient13")
    val strIngredient13: String?,
    @SerializedName("strIngredient14")
    val strIngredient14: String?,
    @SerializedName("strIngredient15")
    val strIngredient15: String?,
    @SerializedName("strMeasure1")
    val strMeasure1: String?,
    @SerializedName("strMeasure2")
    val strMeasure2: String?,
    @SerializedName("strMeasure3")
    val strMeasure3: String?,
    @SerializedName("strMeasure4")
    val strMeasure4: String?,
    @SerializedName("strMeasure5")
    val strMeasure5: String?,
    @SerializedName("strMeasure6")
    val strMeasure6: String?,
    @SerializedName("strMeasure7")
    val strMeasure7: String?,
    @SerializedName("strMeasure8")
    val strMeasure8: String?,
    @SerializedName("strMeasure9")
    val strMeasure9: String?,
    @SerializedName("strMeasure10")
    val strMeasure10: String?,
    @SerializedName("strMeasure11")
    val strMeasure11: String?,
    @SerializedName("strMeasure12")
    val strMeasure12: String?,
    @SerializedName("strMeasure13")
    val strMeasure13: String?,
    @SerializedName("strMeasure14")
    val strMeasure14: String?,
    @SerializedName("strMeasure15")
    val strMeasure15: String?
)


data class Ingredient(
    @SerializedName("strIngredient1")
    val ingredient: String
)

data class IngredientResponse(
    @SerializedName("drinks")
    val drinks: List<Ingredient>?
)


interface CocktailService {
    @GET("search.php")
    suspend fun getCocktailsByLetter(@Query("f") letter: String): ApiResponse

    @GET("search.php")
    suspend fun searchCocktails(@Query("s") query: String): ApiResponse

    @GET("random.php")
    suspend fun getRandomCocktail(): ApiResponse

    
    @GET("filter.php")
    suspend fun getCocktailsByIngredient(@Query("i") ingredient: String): ApiResponse


    @GET("list.php")
    suspend fun getIngredientsList(@Query("i") list: String): IngredientResponse


    @GET("lookup.php")
    suspend fun lookupCocktail(@Query("i") id: String): ApiResponse
}

// Client Retrofit
object ApiClient {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"
    val service: CocktailService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CocktailService::class.java)
    }
}

suspend fun fetchDrinkDetails(id: String): Drink? {
    return withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.service.lookupCocktail(id)
            response.drinks?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

