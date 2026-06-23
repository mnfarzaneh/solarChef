package com.example.solalrchef.data.local

import com.example.solalrchef.model.CookingStep
import com.example.solalrchef.model.Ingredient
import com.example.solalrchef.model.Recipe
import org.json.JSONArray
import org.json.JSONObject

// ─── تبدیل بین Entity و Model ────────────────────────────

fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id           = id,
        title        = title,
        description  = description,
        image        = 0,              // عکس از مسیر فایل لود میشه
        imagePath    = imagePath,
        author       = author,
        source       = source,
        totalTime    = totalTime,
        cookTime     = cookTime,
        yield        = yield,
        calories     = calories,
        difficulty   = difficulty,
        rating       = rating,
        ingredients  = parseIngredients(ingredientsJson),
        steps        = parseSteps(stepsJson),
        equipment    = parseEquipment(equipmentJson)


    )
}

private fun equipmentToJson(equipment: List<String>): String {
    val array = JSONArray()
    equipment.forEach { array.put(it) }
    return array.toString()
}

private fun parseEquipment(json: String): List<String> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { array.getString(it) }
    } catch (e: Exception) { emptyList() }
}

fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
        id               = id,
        title            = title,
        description      = description,
        imagePath        = imagePath ?: "",
        author           = author,
        source           = source,
        totalTime        = totalTime,
        cookTime         = cookTime,
        yield            = yield,
        calories         = calories,
        difficulty       = difficulty,
        rating           = rating,
        ingredientsJson  = ingredientsToJson(ingredients),
        stepsJson        = stepsToJson(steps),
        equipmentJson    = equipmentToJson(equipment)
    )
}

// ─── JSON helpers ─────────────────────────────────────────

private fun ingredientsToJson(ingredients: List<Ingredient>): String {
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

private fun parseIngredients(json: String): List<Ingredient> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Ingredient(
                amount = obj.getString("amount"),
                unit   = obj.getString("unit"),
                name   = obj.getString("name")
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun stepsToJson(steps: List<CookingStep>): String {
    val array = JSONArray()
    steps.forEach { step ->
        array.put(step.instruction)
    }
    return array.toString()
}

private fun parseSteps(json: String): List<CookingStep> {
    if (json.isEmpty()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            CookingStep(instruction = array.getString(i))
        }
    } catch (e: Exception) { emptyList() }
}


