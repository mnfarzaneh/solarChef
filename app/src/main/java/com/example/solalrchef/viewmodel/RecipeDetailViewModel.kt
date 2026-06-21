package com.example.solalrchef.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.solalrchef.data.RecipeRepository
import com.example.solalrchef.data.UserRecipeRepository
import com.example.solalrchef.model.Recipe
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
            val found: Recipe? = RecipeRepository.recipes.firstOrNull { it.id == recipeId }
                ?: userRepo.getRecipeById(recipeId)

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

    fun toggleFavorite() {
        val recipe = _uiState.value.recipe ?: return
        val newFav = !_uiState.value.isFavorite

        // آپدیت فوری UI
        _uiState.update { it.copy(isFavorite = newFav) }

        // ذخیره در دیتابیس (فقط اگه user recipe باشه)
        viewModelScope.launch {
            userRepo.updateFavorite(recipe.id, newFav)
        }
    }

    fun toggleCalculator() {
        _uiState.update { it.copy(showCalculator = !it.showCalculator) }
    }

}