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
import com.mnfarzaneh.solalrchef.data.remote.RecipeParserApi
import com.mnfarzaneh.solalrchef.data.remote.ParseRecipeRequest
import com.mnfarzaneh.solalrchef.util.NetworkMonitor
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException
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
    val equipment: List<String> = listOf(""),
    val isParsing: Boolean = false,
    val parseError: String? = null,
    val parseSuccess: Boolean = false,
)

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository,
    private val recipeParserApi: RecipeParserApi,
    private val networkMonitor: NetworkMonitor,   // ← جایگزین صدا زدن مستقیم تابع شد
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

    fun parseRecipeFromText(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsing = true, parseError = null)

            // ── قدم اول: چک اتصال کلی اینترنت ──
            if (!networkMonitor.isConnected()) {
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    parseError = "به اینترنت متصل نیستید. لطفاً Wi-Fi یا دیتای موبایل را روشن کنید."
                )
                return@launch
            }

            try {
                val result = recipeParserApi.parseRecipe(ParseRecipeRequest(text))
                if (!result.error.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isParsing = false, parseError = result.error)
                    return@launch
                }
                val current = _uiState.value
                _uiState.value = current.copy(
                    title       = result.title.ifBlank { current.title },
                    description = result.description.ifBlank { current.description },
                    totalTime   = result.totalTime.ifBlank { current.totalTime },
                    cookTime    = result.cookTime.ifBlank { current.cookTime },
                    yield       = result.yield.ifBlank { current.yield },
                    calories    = result.calories.ifBlank { current.calories },
                    difficulty  = result.difficulty.ifBlank { current.difficulty },
                    ingredients = if (result.ingredients.isNotEmpty())
                        result.ingredients.map { Ingredient(amount = it.amount, unit = it.unit, name = it.name) }
                    else current.ingredients,
                    steps = if (result.steps.isNotEmpty())
                        result.steps.map { CookingStep(it) }
                    else current.steps,
                    equipment = if (result.equipment.isNotEmpty()) result.equipment else current.equipment,
                    isParsing = false,
                    parseSuccess = true
                )
            } catch (e: SocketTimeoutException) {
                // ── سرور جواب نداد؛ محتمل‌ترین دلیل تو ایران: نیاز به فیلترشکن ──
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    parseError = "پاسخی از سرور دریافت نشد. ممکن است نیاز باشد فیلترشکن (VPN) را روشن کنید."
                )
            } catch (e: UnknownHostException) {
                // ── آدرس سرور اصلاً پیدا نشد؛ این‌هم معمولاً یعنی دسترسی مسدوده ──
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    parseError = "امکان برقراری ارتباط با سرور نبود. اتصال اینترنت یا فیلترشکن را بررسی کنید."
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    parseError = "خطا در ارتباط با سرور. لطفاً دوباره تلاش کنید."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isParsing = false,
                    parseError = "خطای غیرمنتظره: ${e.localizedMessage ?: "نامشخص"}"
                )
            }
        }
    }

    fun clearParseSuccess() {
        _uiState.value = _uiState.value.copy(parseSuccess = false)
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