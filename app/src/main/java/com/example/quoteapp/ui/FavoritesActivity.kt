package com.example.quoteapp.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quoteapp.R
import com.example.quoteapp.adapter.QuoteAdapter
import com.example.quoteapp.data.FavoritesManager

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favoritesManager: FavoritesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        favoritesManager = FavoritesManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val favorites = favoritesManager.getFavoriteQuotes().map {
            it.copy(isFavorite = true)
        }

        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)

        if (favorites.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
            rvFavorites.layoutManager = LinearLayoutManager(this)
            rvFavorites.adapter = QuoteAdapter(
                quotes = favorites,
                onFavoriteClick = { quote ->
                    favoritesManager.removeFavorite(quote)
                }
            )
        }
    }
}