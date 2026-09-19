package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.mnfarzaneh.solalrchef.data.CategoryRepository
import com.mnfarzaneh.solalrchef.data.ContentRepository
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import com.mnfarzaneh.solalrchef.data.RecipeRepository
import com.mnfarzaneh.solalrchef.data.UserRecipeRepository
import com.mnfarzaneh.solalrchef.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRecipeRepository,
    private val categoryRepo: CategoryRepository,
    private val contentRepo: ContentRepository
) : ViewModel() {

    private val _heroArticle = MutableStateFlow<HeroArticleDto?>(null)
    val heroArticle = _heroArticle.asStateFlow()
    private var heroRefreshJob: Job? = null

    init {
        refreshHero()
    }

    fun refreshHero() {
        if (heroRefreshJob?.isActive == true) return

        heroRefreshJob = viewModelScope.launch {
            repeat(HERO_RETRY_COUNT) { attempt ->
                val result = contentRepo.getHero()
                if (result.isSuccess) {
                    _heroArticle.value = result.getOrNull()
                    Log.d("SolarChefContent", "Hero refreshed for public/guest session")
                    return@launch
                }

                Log.w(
                    "SolarChefContent",
                    "Hero request failed (${attempt + 1}/$HERO_RETRY_COUNT)",
                    result.exceptionOrNull()
                )
                if (attempt < HERO_RETRY_COUNT - 1) delay(HERO_RETRY_DELAY_MS)
            }
        }
    }

    private companion object {
        const val HERO_RETRY_COUNT = 3
        const val HERO_RETRY_DELAY_MS = 1_000L
    }

    // ── همه‌ی دستورها: استاتیک (نان/کیک/پیتزا) + دستورهای شخصی کاربر ──
    val allRecipes: StateFlow<List<Recipe>> = combine(
        userRepo.allRecipes,
        userRepo.systemFavoriteIds
    ) { userRecipes, favoriteIds ->
        RecipeRepository.recipes.map { it.copy(isFavorite = it.id in favoriteIds) } + userRecipes
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeRepository.recipes
        )
    val pendingSyncCount: StateFlow<Int> =
        combine(
            userRepo.pendingSyncCount,
            categoryRepo.pendingSyncCount
        ) { recipeCount, categoryCount ->
            recipeCount + categoryCount
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ── فقط علاقه‌مندی‌ها ──
    val favoriteRecipes: StateFlow<List<Recipe>> = allRecipes
        .map { list -> list.filter { it.isFavorite } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refreshFromCloud() {
        viewModelScope.launch {
            userRepo.syncFromCloud()
        }
        refreshHero()
    }
}
