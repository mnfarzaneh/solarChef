package com.mnfarzaneh.solalrchef.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class ParseRecipeRequest(val text: String)

data class ParsedIngredient(
    val amount: String = "",
    val unit: String = "",
    val name: String = ""
)

data class ParsedRecipeResponse(
    val title: String = "",
    val description: String = "",
    val totalTime: String = "",
    val cookTime: String = "",
    val yield: String = "",
    val calories: String = "",
    val difficulty: String = "",
    val ingredients: List<ParsedIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val error: String? = null
)

interface RecipeParserApi {
    @POST("parse-recipe")
    suspend fun parseRecipe(@Body body: ParseRecipeRequest): ParsedRecipeResponse
}