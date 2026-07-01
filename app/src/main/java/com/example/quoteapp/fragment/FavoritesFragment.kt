package com.example.quoteapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quoteapp.R
import com.example.quoteapp.adapter.QuoteAdapter
import com.example.quoteapp.data.FavoritesManager
import com.example.quoteapp.ui.QuoteDetailActivity

class FavoritesFragment : Fragment() {

    private lateinit var favoritesManager: FavoritesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoritesManager = FavoritesManager(requireContext())
        loadFavorites(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadFavorites(it) }
    }

    private fun loadFavorites(view: View) {
        val favorites = favoritesManager.getFavoriteQuotes().map {
            it.copy(isFavorite = true)
        }

        val tvEmpty = view.findViewById<android.widget.TextView>(R.id.tvEmpty)
        val rvFavorites = view.findViewById<RecyclerView>(R.id.rvFavorites)

        if (favorites.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
            rvFavorites.layoutManager = LinearLayoutManager(requireContext())
            rvFavorites.adapter = QuoteAdapter(
                quotes = favorites,
                onFavoriteClick = { quote ->
                    favoritesManager.removeFavorite(quote)
                    loadFavorites(view)
                },
                onQuoteClick = { quote ->
                    val intent = Intent(requireContext(), QuoteDetailActivity::class.java)
                    intent.putExtra("quoteId", quote.id)
                    intent.putExtra("isCustom", quote.isCustom)
                    if (quote.isCustom) {
                        intent.putExtra("customText", quote.text)
                        intent.putExtra("customAuthor", quote.author)
                        intent.putExtra("customCategory", quote.category)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}