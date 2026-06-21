package com.example.solalrchef.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.solalrchef.data.UserRecipeRepository
import com.example.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository
) : ViewModel() {

    val recipes: StateFlow<List<Recipe>> = userRepo.allRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            userRepo.deleteRecipe(recipeId)
        }
    }
}
