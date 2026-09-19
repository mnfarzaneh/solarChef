package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.ContentRepository
import com.mnfarzaneh.solalrchef.data.remote.dto.HeroArticleDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticlesUiState(
    val loading: Boolean = true,
    val articles: List<HeroArticleDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            repository.getArticles()
                .onSuccess { _uiState.value = ArticlesUiState(loading = false, articles = it) }
                .onFailure {
                    _uiState.value = ArticlesUiState(
                        loading = false,
                        error = it.message ?: "دریافت مقاله‌ها ممکن نشد"
                    )
                }
        }
    }
}
