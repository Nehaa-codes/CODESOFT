package com.example.quoteapp.data

import com.example.quoteapp.R

object CategoryTheme {

    fun usesImage(category: String): Boolean {
        return category == "Motivation" || category == "Devotional"
    }

    fun getImageBackground(category: String): Int {
        return when (category) {
            "Motivation" -> R.drawable.bg_motivation
            "Devotional" -> R.drawable.bg_mahabharat
            else -> R.drawable.bg_motivation
        }
    }

    fun getGradientBackground(category: String): Int {
        return when (category) {
            "Life" -> R.drawable.bg_life_gradient
            "Success" -> R.drawable.bg_success_gradient
            "Friendship" -> R.drawable.bg_friendship_gradient
            "Love" -> R.drawable.bg_love_gradient
            "My Quotes" -> R.drawable.bg_favorites_gradient
            else -> R.drawable.bg_life_gradient
        }
    }

    fun getListBackground(category: String): Int {
        return when (category) {
            "Motivation" -> R.drawable.bg_list_motivation
            "Life" -> R.drawable.bg_list_life
            "Success" -> R.drawable.bg_list_success
            "Friendship" -> R.drawable.bg_list_friendship
            "Love" -> R.drawable.bg_list_love
            "Devotional" -> R.drawable.bg_list_mahabharat
            "My Quotes" -> R.drawable.bg_list_myquotes
            else -> R.drawable.bg_list_motivation
        }
    }

    fun getCardColor(category: String): String {
        return when (category) {
            "Motivation" -> "#FFFFFF"
            "Life" -> "#FFE0E9"
            "Success" -> "#FFFFFF"
            "Friendship" -> "#FFFFFF"
            "Love" -> "#F8C4D4"
            "Devotional" -> "#FFD6E0"
            "My Quotes" -> "#FFFFFF"
            else -> "#FFFFFF"
        }
    }

    fun getFont(category: String): Int {
        return when (category) {
            "Motivation" -> R.font.dancing_script
            "Life" -> R.font.dancing_script
            "Success" -> R.font.pacifico
            "Friendship" -> R.font.playfair_display
            "Love" -> R.font.playfair_display
            "Devotional" -> R.font.dancing_script
            else -> R.font.dancing_script
        }
    }
}