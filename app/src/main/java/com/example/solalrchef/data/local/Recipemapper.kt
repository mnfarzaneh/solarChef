package com.mnfarzaneh.solalrchef.data.local

import com.mnfarzaneh.solalrchef.model.CookingStep
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.Recipe
import org.json.JSONArray
import org.json.JSONObject

// ─── تبدیل بین Entity و Model ────────────────────────────

fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id           = id,
        title        = title,
        description  = description,
        image        = 0,
        imagePath    = imagePath,
        imageKey     = imageKey,
        author       = author,
        source       = source,
        totalTime    = totalTime,
        cookTime     = cookTime,
        yield        = yield,
        calories     = calories,
        difficulty   = difficulty,
        rating       = rating,
        isFavorite   = isFavorite,
        ingredients  = parseIngredientsPublic(ingredientsJson),
        steps        = parseStepsPublic(stepsJson),
        equipment    = parseEquipmentPublic(equipmentJson),
        categoryIds  = parseCategoryIds(categoryIdsJson),
        createdAt    = createdAt,
        updatedAt    = updatedAt
    )
}

fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
        id               = id,
        title            = title,
        description      = description,
        imagePath        = imagePath ?: "",
        imageKey         = imageKey,
        author           = author,
        source           = source,
        totalTime        = totalTime,
        cookTime         = cookTime,
        yield            = yield,
        calories         = calories,
        difficulty       = difficulty,
        rating           = rating,
        isFavorite       = isFavorite,
        ingredientsJson  = ingredientsToJsonPublic(ingredients),
        stepsJson        = stepsToJsonPublic(steps),
        equipmentJson    = equipmentToJsonPublic(equipment),
        categoryIdsJson  = categoryIdsToJson(categoryIds),
        ownerId          = null,
        createdAt        = createdAt,
        updatedAt        = updatedAt
    )
}

// ─── توابع عمومی (public) برای استفاده در SolarChefApiRepository ──

fun ingredientsToJsonPublic(ingredients: List<Ingredient>): String {
    val array = JSONArray()
    ingredients.forEach { ing ->
        val obj = JSONObject()
        obj.put("amount", ing.amount)
        obj.put("unit", ing.unit)
        obj.put("name", ing.name)
        array.put(obj)
    }
    return array.toString()
}

fun parseIngredientsPublic(json: String): List<Ingredient> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Ingredient(
                amount = obj.optString("amount", ""),
                unit   = obj.optString("unit", ""),
                name   = obj.optString("name", "")
            )
        }
    } catch (e: Exception) { emptyList() }
}

fun stepsToJsonPublic(steps: List<CookingStep>): String {
    val array = JSONArray()
    steps.forEach { array.put(it.instruction) }
    return array.toString()
}

fun parseStepsPublic(json: String): List<CookingStep> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            CookingStep(instruction = array.getString(i))
        }
    } catch (e: Exception) { emptyList() }
}

fun equipmentToJsonPublic(equipment: List<String>): String {
    val array = JSONArray()
    equipment.forEach { array.put(it) }
    return array.toString()
}

fun parseEquipmentPublic(json: String): List<String> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { array.getString(it) }
    } catch (e: Exception) { emptyList() }
}

fun categoryIdsToJson(ids: List<String>): String {
    val array = JSONArray()
    ids.forEach { array.put(it) }
    return array.toString()
}

fun parseCategoryIds(json: String): List<String> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { array.getString(it) }
    } catch (e: Exception) { emptyList() }
}
