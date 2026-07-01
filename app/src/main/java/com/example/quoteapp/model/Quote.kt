package com.example.quoteapp.model

data class Quote(
    val id: Int,
    val text: String,
    val author: String,
    val category: String,
    var isFavorite: Boolean = false,
    val isCustom: Boolean = false
)