package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.RecipeRepository
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository
) : ViewModel() {

    // ── همه‌ی دستورها: استاتیک (نان/کیک/پیتزا) + دستورهای شخصی کاربر ──
    val allRecipes: StateFlow<List<Recipe>> = userRepo.allRecipes
        .map { userRecipes -> RecipeRepository.recipes + userRecipes }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeRepository.recipes
        )

    // ── فقط علاقه‌مندی‌ها ──
    val favoriteRecipes: StateFlow<List<Recipe>> = allRecipes
        .map { list -> list.filter { it.isFavorite } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}