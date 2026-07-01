package com.example.quoteapp.data

import android.content.Context
import com.example.quoteapp.model.Quote

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun saveFavorite(quote: Quote) {
        val set = getFavoriteIds().toMutableSet()
        set.add(quote.id.toString())
        prefs.edit().putStringSet("fav_ids", set).apply()
    }

    fun removeFavorite(quote: Quote) {
        val set = getFavoriteIds().toMutableSet()
        set.remove(quote.id.toString())
        prefs.edit().putStringSet("fav_ids", set).apply()
    }

    fun isFavorite(quote: Quote): Boolean {
        return getFavoriteIds().contains(quote.id.toString())
    }

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet("fav_ids", emptySet()) ?: emptySet()
    }

    fun getFavoriteQuotes(): List<Quote> {
        val ids = getFavoriteIds()
        return QuoteData.quotes.filter { ids.contains(it.id.toString()) }
    }
}