package com.mnfarzaneh.solalrchef.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.model.CookingStep
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddRecipeUiState(
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val totalTime: String = "",
    val cookTime: String = "",
    val yield: String = "4",
    val calories: String = "0",
    val difficulty: String = "متوسط",
    val imageUri: Uri? = null,
    val ingredients: List<Ingredient> = listOf(Ingredient("", "", "")),
    val steps: List<CookingStep> = listOf(CookingStep("")),
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val equipment: List<String> = listOf("")
)

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val editRecipeId: String? = savedStateHandle["recipeId"]
    // بقیه کد...

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState

    // ── اگه ویرایشه، داده‌های قبلی رو لود کن ──
    init {
        if (editRecipeId != null) {
            loadExistingRecipe(editRecipeId)
        }
    }

    private fun loadExistingRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val recipe = userRepo.getRecipeById(recipeId)
            recipe?.let {
                _uiState.value = _uiState.value.copy(
                    title       = it.title,
                    description = it.description,
                    author      = it.author,
                    totalTime   = it.totalTime,
                    cookTime    = it.cookTime,
                    yield       = it.yield,
                    calories    = it.calories.toString(),
                    difficulty  = it.difficulty,
                    imageUri    = if (it.imagePath.isNotEmpty()) Uri.parse(it.imagePath) else null,
                    ingredients = it.ingredients.ifEmpty { listOf(Ingredient("", "", "")) },
                    steps       = it.steps.ifEmpty { listOf(CookingStep("")) },
                    isLoading   = false,
                    equipment = it.equipment.ifEmpty { listOf("") },
                )
            }
        }
    }
    private fun copyImageToInternalStorage(uri: Uri): String {
        return try {
            val context = application
            val fileName = "recipe_${System.currentTimeMillis()}.jpg"
            val destFile = java.io.File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            uri.toString()
        }
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(
            title = value,
            error = null
        )
    }    fun updateDescription(value: String) { _uiState.value = _uiState.value.copy(description = value) }
    fun updateAuthor(value: String)      { _uiState.value = _uiState.value.copy(author = value) }
    fun updateTotalTime(value: String)   { _uiState.value = _uiState.value.copy(totalTime = value) }
    fun updateCookTime(value: String)    { _uiState.value = _uiState.value.copy(cookTime = value) }
    fun updateYield(value: String)       { _uiState.value = _uiState.value.copy(yield = value) }
    fun updateCalories(value: String)    { _uiState.value = _uiState.value.copy(calories = value) }
    fun updateDifficulty(value: String)  { _uiState.value = _uiState.value.copy(difficulty = value) }
    fun updateImageUri(uri: Uri?)        { _uiState.value = _uiState.value.copy(imageUri = uri) }

    fun updateIngredient(index: Int, ingredient: Ingredient) {
        val list = _uiState.value.ingredients.toMutableList()
        list[index] = ingredient
        _uiState.value = _uiState.value.copy(ingredients = list)
    }
    fun addIngredient() {
        val list = _uiState.value.ingredients.toMutableList()
        list.add(Ingredient("", "", ""))
        _uiState.value = _uiState.value.copy(ingredients = list)
    }
    fun removeIngredient(index: Int) {
        if (_uiState.value.ingredients.size <= 1) return
        val list = _uiState.value.ingredients.toMutableList()
        list.removeAt(index)
        _uiState.value = _uiState.value.copy(ingredients = list)
    }

    fun updateStep(index: Int, instruction: String) {
        val list = _uiState.value.steps.toMutableList()
        list[index] = CookingStep(instruction)
        _uiState.value = _uiState.value.copy(steps = list)
    }
    fun addStep() {
        val list = _uiState.value.steps.toMutableList()
        list.add(CookingStep(""))
        _uiState.value = _uiState.value.copy(steps = list)
    }
    fun removeStep(index: Int) {
        if (_uiState.value.steps.size <= 1) return
        val list = _uiState.value.steps.toMutableList()
        list.removeAt(index)
        _uiState.value = _uiState.value.copy(steps = list)
    }

    fun saveRecipe() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "عنوان دستور را وارد کنید")
            return
        }

        viewModelScope.launch {
            val recipe = Recipe(
                // اگه ویرایشه همون id رو نگه دار، وگرنه id جدید بساز
                id          = editRecipeId ?: "user_${UUID.randomUUID()}",
                title       = state.title,
                description = state.description,
                image       = 0,
                imagePath = state.imageUri?.let { uri ->
                    // اگه قبلاً کپی شده (path داخلیه)، دوباره کپی نکن
                    if (uri.toString().startsWith("/")) uri.toString()
                    else copyImageToInternalStorage(uri)
                } ?: "",
                author      = state.author.ifBlank { "من" },
                source      = "دستور شخصی",
                totalTime   = state.totalTime,
                cookTime    = state.cookTime,
                yield       = state.yield,
                calories    = state.calories.toIntOrNull() ?: 0,
                difficulty  = state.difficulty,
                rating      = 5f,
                ingredients = state.ingredients.filter { it.name.isNotBlank() },
                steps       = state.steps.filter { it.instruction.isNotBlank() },
                equipment = state.equipment.filter { it.isNotBlank() }

            )
            userRepo.saveRecipe(recipe)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
    fun updateEquipment(index: Int, value: String) {
        val list = _uiState.value.equipment.toMutableList()
        list[index] = value
        _uiState.value = _uiState.value.copy(equipment = list)
    }

    fun addEquipment() {
        val list = _uiState.value.equipment.toMutableList()
        list.add("")
        _uiState.value = _uiState.value.copy(equipment = list)
    }

    fun removeEquipment(index: Int) {
        if (_uiState.value.equipment.size <= 1) return

        val list = _uiState.value.equipment.toMutableList()
        list.removeAt(index)

        _uiState.value = _uiState.value.copy(equipment = list)
    }


}