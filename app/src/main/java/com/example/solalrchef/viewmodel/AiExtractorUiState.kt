package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.data.remote.RecipeParserApi
import com.mnfarzaneh.solalrchef.model.CookingStep
import com.mnfarzaneh.solalrchef.model.Ingredient
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiExtractorUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val parsedTitle: String? = null   // نشون میده parse موفق بود
)

@HiltViewModel
class AiRecipeExtractorViewModel @Inject constructor(
    private val parserApi: RecipeParserApi,
    private val userRepo: UserRecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiExtractorUiState())
    val uiState: StateFlow<AiExtractorUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, error = null) }
    }

    fun extractAndSave() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(error = "متن دستور را وارد کنید") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val parsed = parserApi.parseRecipe(
                    com.mnfarzaneh.solalrchef.data.remote.ParseRecipeRequest(text)
                )

                if (parsed.error != null) {
                    _uiState.update { it.copy(isLoading = false, error = parsed.error) }
                    return@launch
                }

                if (parsed.title.isBlank()) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "دستوری از متن استخراج نشد")
                    }
                    return@launch
                }

                val recipe = Recipe(
                    id          = "user_${UUID.randomUUID()}",
                    title       = parsed.title,
                    description = parsed.description,
                    image       = 0,
                    imagePath   = "",
                    author      = "من",
                    source      = "دستور شخصی",
                    totalTime   = parsed.totalTime,
                    cookTime    = parsed.cookTime,
                    yield       = parsed.yield.ifBlank { "4" },
                    calories    = parsed.calories.toIntOrNull() ?: 0,
                    difficulty  = parsed.difficulty.ifBlank { "متوسط" },
                    rating      = 5f,
                    ingredients = parsed.ingredients.map {
                        Ingredient(amount = it.amount, unit = it.unit, name = it.name)
                    },
                    steps       = parsed.steps.map { CookingStep(it) },
                    equipment   = parsed.equipment,
                    categoryIds = if (parsed.categoryId.isNotBlank())
                        listOf(parsed.categoryId) else emptyList()
                )

                userRepo.saveRecipe(recipe)
                _uiState.update {
                    it.copy(isLoading = false, isSaved = true, parsedTitle = parsed.title)
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "خطا در اتصال به سرور")
                }
            }
        }
    }

    fun onSaveHandled() {
        _uiState.update { it.copy(isSaved = false, parsedTitle = null, inputText = "") }
    }

    fun onErrorHandled() {
        _uiState.update { it.copy(error = null) }
    }
}