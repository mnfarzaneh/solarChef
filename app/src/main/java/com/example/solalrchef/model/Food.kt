package com.example.solalrchef.model

import androidx.compose.ui.unit.DpOffset


data class Food(
    val id: String,
    val image: Int,
    val start: DpOffset,
    val end: DpOffset
)