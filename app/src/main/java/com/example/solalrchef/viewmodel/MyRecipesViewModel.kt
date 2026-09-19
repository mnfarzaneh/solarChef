package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository
) : ViewModel() {

    val recipes: StateFlow<List<com.mnfarzaneh.solalrchef.model.Recipe>> =
        userRepo.allRecipes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshFromCloud() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                userRepo.syncFromCloud()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            userRepo.deleteRecipe(recipeId)
        }
    }
}