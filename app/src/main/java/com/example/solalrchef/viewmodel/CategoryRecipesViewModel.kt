package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.SavedStateHandle
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

// ← نگاشت هر دسته‌بندیِ seed‌شده به لیست دستورهای استاتیکی که توش قرار می‌گیرن.
// (نه یه نگاشت تک‌به‌تک — چون مثلاً «نان پیتزا» باید هم توی «نانها» باشه هم «پیتزا»)
// اگه بعداً دستور استاتیک دیگه‌ای اضافه کردی، همینجا بهش category بده.
val staticCategoryRecipeIds: Map<String, List<String>> = mapOf(
    "cat_bread" to listOf("bread", "pizza"),   // نانها: نان خانگی + نان پیتزا
    "cat_pizza" to listOf("pizza"),             // پیتزا: نان پیتزا
    "cat_cake" to listOf("cake"),               // شیرینی‌جات: کیک اسفنجی
)

@HiltViewModel
class CategoryRecipesViewModel @Inject constructor(
    private val userRecipeRepository: UserRecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val categoryId: String = savedStateHandle["categoryId"] ?: ""

    val recipes: StateFlow<List<Recipe>> = userRecipeRepository.allRecipes
        .map { userRecipes ->
            val staticIds = staticCategoryRecipeIds[categoryId] ?: emptyList()
            val staticRecipes = RecipeRepository.recipes.filter { it.id in staticIds }
            val userRecipesInCategory = userRecipes.filter { categoryId in it.categoryIds }
            staticRecipes + userRecipesInCategory
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            // ← اینجا مقدار اولیه رو با دستورات استاتیک پر کن
            initialValue = run {
                val staticIds = staticCategoryRecipeIds[categoryId] ?: emptyList()
                RecipeRepository.recipes.filter { it.id in staticIds }
            }
        )
}