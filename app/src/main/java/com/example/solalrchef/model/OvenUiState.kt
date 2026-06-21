package com.example.solalrchef.model

data class OvenUiState(
    val isOpen: Boolean = false,
    val foods: List<Food> = emptyList()
)