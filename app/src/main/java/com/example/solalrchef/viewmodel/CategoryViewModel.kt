package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.CategoryRepository
import com.mnfarzaneh.solalrchef.data.RecipeRepository
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.model.Category
import com.mnfarzaneh.solalrchef.viewmodel.staticCategoryRecipeIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

data class CategoryWithCount(
    val category: Category,
    val recipeCount: Int
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val userRecipeRepository: UserRecipeRepository
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    val categoriesWithCounts: StateFlow<List<CategoryWithCount>> =
        combine(categoryRepository.categories, userRecipeRepository.allRecipes) { categories, userRecipes ->
            categories.map { category ->
                // ← دستورهای استاتیک (از نگاشت چندبه‌چند) + دستورهای کاربر
                // (که categoryIds‌شون شامل این دسته باشه)
                val staticCount = (staticCategoryRecipeIds[category.id] ?: emptyList()).size
                val userCount = userRecipes.count { category.id in it.categoryIds }
                CategoryWithCount(category = category, recipeCount = staticCount + userCount)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        viewModelScope.launch {
            categoryRepository.ensureDefaultCategoriesExist()
        }
    }

    fun refreshFromCloud() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                // ابتدا تغییرات ثبت‌نشده را ارسال می‌کنیم،
                // بعد نسخهٔ نهایی را از سرور می‌خوانیم.
                categoryRepository.syncPendingOperations()
                categoryRepository.syncFromCloud()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    fun addCategory(name: String, emoji: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.addCategory(name.trim(), emoji)
        }
    }

    fun updateCategory(id: String, name: String, emoji: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.updateCategory(id, name.trim(), emoji)
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id)
        }
    }
}