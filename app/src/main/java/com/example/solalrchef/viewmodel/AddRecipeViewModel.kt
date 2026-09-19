package com.mnfarzaneh.solalrchef.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.data.CategoryRepository
import com.mnfarzaneh.solalrchef.model.CookingStep
import com.mnfarzaneh.solalrchef.model.Category
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.mnfarzaneh.solalrchef.data.remote.RecipeParserApi
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.data.remote.ParseRecipeRequest
import com.mnfarzaneh.solalrchef.util.NetworkMonitor
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import retrofit2.HttpException

data class AddRecipeUiState(
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val totalTime: String = "",
    val cookTime: String = "",
    val yield: String = "",
    val calories: String = "0",
    val difficulty: String = "متوسط",
    val imageUri: Uri? = null,
    val imageKey: String = "",
    val ingredients: List<Ingredient> = listOf(Ingredient("", "", "")),
    val steps: List<CookingStep> = listOf(CookingStep("")),
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val equipment: List<String> = listOf(""),
    val isParsing: Boolean = false,
    val parseSuccess: Boolean = false,
    val categoryIds: List<String> = emptyList(),  // ← تغییر کرد: می‌تونه چند دسته باشه
    val categories: List<Category> = emptyList(), // ← جدید
)

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository,
    private val categoryRepository: CategoryRepository,
    private val recipeParserApi: RecipeParserApi,
    private val networkMonitor: NetworkMonitor,   // ← جایگزین صدا زدن مستقیم تابع شد
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val editRecipeId: String? =
        savedStateHandle["recipeId"]
            ?: savedStateHandle["editRecipeId"]
    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState

    // ── کانال رویدادهای یک‌بار مصرف (Snackbar/Toast) ──
    // با Channel پیام فقط یک‌بار consume می‌شه، پس با recomposition
    // یا چرخش صفحه دیگه دوباره نمایش داده نمی‌شه.
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch { _uiEvent.send(UiEvent.ShowMessage(message)) }
    }

    // ── اگه ویرایشه، داده‌های قبلی رو لود کن ──
    init {
        if (editRecipeId != null) {
            loadExistingRecipe(editRecipeId)
        }
        viewModelScope.launch {
            categoryRepository.categories.collect { list ->
                _uiState.value = _uiState.value.copy(categories = list)
            }
        }
    }

    fun toggleCategory(categoryId: String) {
        val current = _uiState.value.categoryIds
        val updated = if (categoryId in current) current - categoryId else current + categoryId
        _uiState.value = _uiState.value.copy(categoryIds = updated)
    }

    fun addAndSelectCategory(name: String, emoji: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        if (_uiState.value.categories.any { it.name.trim().equals(normalizedName, ignoreCase = true) }) {
            sendMessage(application.getString(R.string.category_already_exists))
            return
        }

        viewModelScope.launch {
            val categoryId = categoryRepository.addCategory(
                name = normalizedName,
                emoji = emoji.trim().ifBlank { "🍽️" }
            )
            _uiState.value = _uiState.value.copy(
                categoryIds = (_uiState.value.categoryIds + categoryId).distinct()
            )
            sendMessage(application.getString(R.string.category_created_and_selected))
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
                    imageKey    = it.imageKey,
                    ingredients = it.ingredients.ifEmpty { listOf(Ingredient("", "", "")) },
                    steps       = it.steps.ifEmpty { listOf(CookingStep("")) },
                    isLoading   = false,
                    equipment   = it.equipment.ifEmpty { listOf("") },
                    categoryIds = it.categoryIds,
                )
            }
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val extension = when (application.contentResolver.getType(uri)) {
                    "image/png" -> ".png"
                    "image/webp" -> ".webp"
                    else -> ".jpg"
                }

                val imagesDirectory = File(application.filesDir, "recipe_images")
                imagesDirectory.mkdirs()

                val destination = File(
                    imagesDirectory,
                    "recipe_${System.currentTimeMillis()}$extension"
                )

                application.contentResolver.openInputStream(uri)?.use { input ->
                    destination.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext null

                destination.absolutePath
            } catch (_: Exception) {
                null
            }
        }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }
    fun updateDescription(value: String) { _uiState.value = _uiState.value.copy(description = value) }
    fun updateAuthor(value: String)      { _uiState.value = _uiState.value.copy(author = value) }
    fun updateTotalTime(value: String)   { _uiState.value = _uiState.value.copy(totalTime = value) }
    fun updateCookTime(value: String)    { _uiState.value = _uiState.value.copy(cookTime = value) }
    fun updateYield(value: String)       { _uiState.value = _uiState.value.copy(yield = value) }
    fun updateCalories(value: String)    { _uiState.value = _uiState.value.copy(calories = value) }
    fun updateDifficulty(value: String)  { _uiState.value = _uiState.value.copy(difficulty = value) }
    fun updateImageUri(uri: Uri?) {
        if (uri == null) {
            _uiState.value = _uiState.value.copy(
                imageUri = null,
                imageKey = ""
            )
            return
        }

        viewModelScope.launch {
            val localPath = copyImageToInternalStorage(uri)

            if (localPath == null) {
                sendMessage("ذخیره‌سازی عکس روی گوشی ناموفق بود")
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                imageUri = Uri.parse(localPath),
                imageKey = ""
            )
        }
    }
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

        if (state.isSaving) return   // ← جلوگیری از دوبار ذخیره‌ی هم‌زمان

        if (state.title.isBlank()) {
            sendMessage("عنوان دستور را وارد کنید")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val recipe = Recipe(
                // اگه ویرایشه همون id رو نگه دار، وگرنه id جدید بساز
                id          = editRecipeId ?: "user_${UUID.randomUUID()}",
                title       = state.title,
                description = state.description,
                image       = 0,
                imagePath = state.imageUri?.let { uri ->
                    val uriString = uri.toString()

                    if (uriString.startsWith("/") || uriString.startsWith("http")) {
                        uriString
                    } else {
                        copyImageToInternalStorage(uri).orEmpty()
                    }
                }.orEmpty(),
                imageKey    = state.imageKey,
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
                equipment   = state.equipment.filter { it.isNotBlank() },
                categoryIds = state.categoryIds
            )
            try {
                userRepo.saveRecipe(recipe)

                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    isSaving = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false
                )

                sendMessage(
                    "ذخیره‌سازی ناموفق بود: ${e.localizedMessage ?: "خطای نامشخص"}"
                )
            }
        }
    }

    fun parseRecipeFromText(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsing = true)

            // ── قدم اول: چک اتصال کلی اینترنت ──
            if (!networkMonitor.isConnected()) {
                _uiState.value = _uiState.value.copy(isParsing = false)
                sendMessage("به اینترنت متصل نیستید. لطفاً Wi-Fi یا دیتای موبایل را روشن کنید.")
                return@launch
            }

            try {
                val result = recipeParserApi.parseRecipe(ParseRecipeRequest(text))
                if (!result.error.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isParsing = false)
                    sendMessage(result.error)
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
                    categoryIds = if (result.categoryId.isNotBlank())
                        listOf(result.categoryId)
                    else current.categoryIds,
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
                _uiState.value = _uiState.value.copy(isParsing = false)
                sendMessage(application.getString(com.mnfarzaneh.solalrchef.R.string.recipe_extraction_connection_error))
            } catch (e: UnknownHostException) {
                // ── آدرس سرور اصلاً پیدا نشد؛ این‌هم معمولاً یعنی دسترسی مسدوده ──
                _uiState.value = _uiState.value.copy(isParsing = false)
                sendMessage(application.getString(com.mnfarzaneh.solalrchef.R.string.recipe_extraction_connection_error))
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(isParsing = false)
                val message = if (e.code() in listOf(500, 502, 503, 504)) {
                    application.getString(com.mnfarzaneh.solalrchef.R.string.recipe_extraction_server_error)
                } else {
                    application.getString(com.mnfarzaneh.solalrchef.R.string.recipe_extraction_connection_error)
                }
                sendMessage(message)
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(isParsing = false)
                sendMessage(application.getString(com.mnfarzaneh.solalrchef.R.string.recipe_extraction_connection_error))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isParsing = false)
                sendMessage("خطای غیرمنتظره: ${e.localizedMessage ?: "نامشخص"}")
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
