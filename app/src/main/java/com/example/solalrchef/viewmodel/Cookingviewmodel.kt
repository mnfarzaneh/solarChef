package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.RecipeRepository
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────
data class CookingUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val servings: Int = 4,
    val completedSteps: List<Boolean> = emptyList(),
    val showCalculator: Boolean = false
)

// CookingViewModel
@HiltViewModel
class CookingViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository,
    savedStateHandle: SavedStateHandle  // ← برای recipeId
) : ViewModel() {

    private val recipeId: String = savedStateHandle["recipeId"] ?: ""

    private val _uiState = MutableStateFlow(CookingUiState())
    val uiState: StateFlow<CookingUiState> = _uiState.asStateFlow()

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            // اول از منابع استاتیک چک کن
            val staticRecipe = RecipeRepository.recipes.firstOrNull { it.id == recipeId }
            if (staticRecipe != null) {
                initWithRecipe(staticRecipe)
                return@launch
            }

            // بعد از Room بخون
            val userRecipe = userRepo.getRecipeById(recipeId)
            if (userRecipe != null) {
                initWithRecipe(userRecipe)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun initWithRecipe(recipe: Recipe) {
        _uiState.update {
            it.copy(
                recipe = recipe,
                isLoading = false,
                servings = recipe.yield.toIntOrNull() ?: 4,
                completedSteps = List(recipe.steps.size) { false }
            )
        }
    }

    // ── تب ───────────────────────────────────────────────
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // ── وعده ─────────────────────────────────────────────
    fun incrementServings() {
        _uiState.update { it.copy(servings = it.servings + 1) }
    }

    fun decrementServings() {
        if (_uiState.value.servings > 1) {
            _uiState.update { it.copy(servings = it.servings - 1) }
        }
    }

    // ── مراحل ────────────────────────────────────────────
    fun toggleStep(index: Int) {
        val current = _uiState.value.completedSteps.toMutableList()
        if (index in current.indices) {
            current[index] = !current[index]
            _uiState.update { it.copy(completedSteps = current) }
        }
    }

    // ── ماشین حساب ───────────────────────────────────────
    fun showCalculator() {
        _uiState.update { it.copy(showCalculator = true) }
    }

    fun hideCalculator() {
        _uiState.update { it.copy(showCalculator = false) }
    }
}



