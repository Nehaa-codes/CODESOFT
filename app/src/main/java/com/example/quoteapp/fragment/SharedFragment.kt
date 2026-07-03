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
import com.example.quoteapp.data.SharedQuoteManager
import com.example.quoteapp.ui.QuoteDetailActivity

class SharedFragment : Fragment() {

    private lateinit var sharedQuoteManager: SharedQuoteManager
    private lateinit var favoritesManager: FavoritesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_shared, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedQuoteManager = SharedQuoteManager(requireContext())
        favoritesManager = FavoritesManager(requireContext())
        loadShared(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadShared(it) }
    }

    private fun loadShared(view: View) {
        val shared = sharedQuoteManager.getSharedQuotes().map {
            it.copy(isFavorite = favoritesManager.isFavorite(it))
        }

        val tvEmpty = view.findViewById<android.widget.TextView>(R.id.tvEmptyShared)
        val rvShared = view.findViewById<RecyclerView>(R.id.rvShared)

        if (shared.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvShared.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvShared.visibility = View.VISIBLE
            rvShared.layoutManager = LinearLayoutManager(requireContext())
            rvShared.adapter = QuoteAdapter(
                quotes = shared,
                onFavoriteClick = { quote ->
                    if (quote.isFavorite) {
                        favoritesManager.saveFavorite(quote)
                    } else {
                        favoritesManager.removeFavorite(quote)
                    }
                    loadShared(view)
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