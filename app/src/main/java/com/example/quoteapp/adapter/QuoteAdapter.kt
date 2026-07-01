package com.example.quoteapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.quoteapp.R
import com.example.quoteapp.model.Quote

class QuoteAdapter(
    private val quotes: List<Quote>,
    private val onFavoriteClick: (Quote) -> Unit,
    private val onQuoteClick: (Quote) -> Unit = {},
    private val onQuoteLongClick: (Quote) -> Unit = {},
    private val cardColor: String? = null
) : RecyclerView.Adapter<QuoteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardQuote: CardView = view.findViewById(R.id.cardQuote)
        val tvQuoteText: TextView = view.findViewById(R.id.tvQuoteText)
        val tvQuoteAuthor: TextView = view.findViewById(R.id.tvQuoteAuthor)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quote = quotes[position]
        holder.tvQuoteText.text = "❝ ${quote.text} ❞"
        holder.tvQuoteAuthor.text = "— ${quote.author}"

        if (quote.isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
            holder.cardQuote.setCardBackgroundColor(Color.parseColor("#FFF3CD"))
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_outline)
            val baseColor = cardColor ?: "#FFFFFF"
            holder.cardQuote.setCardBackgroundColor(Color.parseColor(baseColor))
        }

        holder.btnFavorite.setOnClickListener {
            quote.isFavorite = !quote.isFavorite
            onFavoriteClick(quote)
            notifyItemChanged(position)
        }

        holder.cardQuote.setOnClickListener {
            onQuoteClick(quote)
        }

        holder.cardQuote.setOnLongClickListener {
            if (quote.isCustom) {
                onQuoteLongClick(quote)
            }
            true
        }
    }

    override fun getItemCount() = quotes.size
}