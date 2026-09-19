package com.mnfarzaneh.solalrchef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnfarzaneh.solalrchef.data.ContentRepository
import com.mnfarzaneh.solalrchef.data.remote.dto.ArticleDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleUiState(
    val loading: Boolean = true,
    val article: ArticleDto? = null,
    val error: String? = null
)

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState = _uiState.asStateFlow()

    fun load(slug: String) {
        if (_uiState.value.article?.slug == slug) return
        viewModelScope.launch {
            _uiState.value = ArticleUiState(loading = true)
            repository.getArticle(slug)
                .onSuccess { _uiState.value = ArticleUiState(loading = false, article = it) }
                .onFailure {
                    _uiState.value = ArticleUiState(
                        loading = false,
                        error = it.message ?: "خطا در دریافت مقاله"
                    )
                }
        }
    }
}
