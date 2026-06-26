package com.mnfarzaneh.solalrchef.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mnfarzaneh.solalrchef.R
import com.mnfarzaneh.solalrchef.model.FoodItem
import com.mnfarzaneh.solalrchef.model.OvenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OvenViewModel : ViewModel() {

    var animationCompleted by mutableStateOf(false)
        private set

    fun markAnimationCompleted() {
        animationCompleted = true
    }

    val foods = listOf(
        FoodItem(
            id = "bread",
            insideImage = R.drawable.bread,
            outsideImage = R.drawable.bredcircle
        ),
        FoodItem(
            id = "pizza",
            insideImage = R.drawable.pizza,
            outsideImage = R.drawable.pizzacircle
        ),
        FoodItem(
            id = "cake",
            insideImage = R.drawable.cake,
            outsideImage = R.drawable.cakecircle
        )
    )
    private val _uiState = MutableStateFlow(OvenUiState())

    val uiState = _uiState.asStateFlow()

    fun toggleOven() {
        _uiState.update {
            it.copy(isOpen = !it.isOpen)
        }
    }
}