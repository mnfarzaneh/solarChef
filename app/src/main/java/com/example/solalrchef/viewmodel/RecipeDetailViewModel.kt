package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.RecipeRepository
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────
data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isFavorite: Boolean = false,
    val showCalculator: Boolean = false,
    val showShoppingList: Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // اول از RecipeRepository استاتیک چک کن
            val systemRecipe = RecipeRepository.recipes.firstOrNull { it.id == recipeId }
            val found: Recipe? = systemRecipe?.copy(
                isFavorite = userRepo.isSystemFavorite(recipeId)
            ) ?: userRepo.getRecipeById(recipeId)

            if (found == null) {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
            } else {
                _uiState.update {
                    it.copy(
                        recipe = found,
                        isLoading = false,
                        isFavorite = found.isFavorite   // مقدار اولیه از مدل
                    )
                }
            }
        }
    }
    fun showShoppingList() {
        _uiState.update {
            it.copy(showShoppingList = true)
        }
    }

    fun hideShoppingList() {
        _uiState.update {
            it.copy(showShoppingList = false)
        }
    }

    fun toggleFavorite() {
        val recipe = _uiState.value.recipe ?: return
        val newFav = !_uiState.value.isFavorite

        // آپدیت فوری UI
        _uiState.update { it.copy(isFavorite = newFav) }

        val isSystemRecipe = RecipeRepository.recipes.any { it.id == recipe.id }
        viewModelScope.launch {
            if (isSystemRecipe) {
                userRepo.updateSystemFavorite(recipe.id, newFav)
            } else {
                userRepo.updateFavorite(recipe.id, newFav)
            }
        }
    }

    fun toggleCalculator() {
        _uiState.update { it.copy(showCalculator = !it.showCalculator) }
    }

}
