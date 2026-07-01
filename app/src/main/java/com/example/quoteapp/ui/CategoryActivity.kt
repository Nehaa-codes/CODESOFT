package com.example.quoteapp.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quoteapp.R
import com.example.quoteapp.adapter.QuoteAdapter
import com.example.quoteapp.data.CategoryTheme
import com.example.quoteapp.data.CustomQuoteManager
import com.example.quoteapp.data.FavoritesManager
import com.example.quoteapp.data.QuoteData
import com.example.quoteapp.model.Quote

class CategoryActivity : AppCompatActivity() {

    private lateinit var favoritesManager: FavoritesManager
    private lateinit var customQuoteManager: CustomQuoteManager
    private lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        favoritesManager = FavoritesManager(this)
        customQuoteManager = CustomQuoteManager(this)

        category = intent.getStringExtra("category") ?: "Motivation"
        findViewById<TextView>(R.id.tvCategoryTitle).text = category

        findViewById<RelativeLayout>(R.id.categoryRoot)
            .setBackgroundResource(CategoryTheme.getListBackground(category))

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val btnAddQuote = findViewById<Button>(R.id.btnAddQuote)
        if (category == "My Quotes") {
            btnAddQuote.visibility = android.view.View.VISIBLE
            btnAddQuote.setOnClickListener { showAddQuoteDialog() }
        } else {
            btnAddQuote.visibility = android.view.View.GONE
        }

        loadQuotes()
    }
    override fun onResume() {
        super.onResume()
        loadQuotes()
    }

    private fun loadQuotes() {
        val allQuotes: List<Quote> = if (category == "My Quotes") {
            customQuoteManager.getCustomQuotes()
        } else {
            QuoteData.getByCategory(category)
        }.map {
            it.copy(isFavorite = favoritesManager.isFavorite(it))
        }

        val rvQuotes = findViewById<RecyclerView>(R.id.rvQuotes)
        rvQuotes.layoutManager = LinearLayoutManager(this)
        rvQuotes.adapter = QuoteAdapter(
            quotes = allQuotes,
            cardColor = CategoryTheme.getCardColor(category),
            onFavoriteClick = { quote ->
                if (quote.isFavorite) {
                    favoritesManager.saveFavorite(quote)
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                } else {
                    favoritesManager.removeFavorite(quote)
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                }
            },
            onQuoteClick = { quote ->
                val intent = Intent(this, QuoteDetailActivity::class.java)
                intent.putExtra("quoteId", quote.id)
                intent.putExtra("isCustom", quote.isCustom)
                if (quote.isCustom) {
                    intent.putExtra("customText", quote.text)
                    intent.putExtra("customAuthor", quote.author)
                    intent.putExtra("customCategory", quote.category)
                }
                startActivity(intent)
            },
            onQuoteLongClick = { quote ->
                showEditDeleteDialog(quote)
            }
        )
    }

    private fun showEditDeleteDialog(quote: Quote) {
        AlertDialog.Builder(this)
            .setTitle("Manage Quote")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> showEditQuoteDialog(quote)
                    1 -> {
                        customQuoteManager.deleteQuote(quote.id)
                        favoritesManager.removeFavorite(quote)
                        loadQuotes()
                        Toast.makeText(this, "Quote deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun showEditQuoteDialog(quote: Quote) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)

        val quoteInput = EditText(this)
        quoteInput.setText(quote.text)
        layout.addView(quoteInput)

        val authorInput = EditText(this)
        authorInput.setText(quote.author)
        layout.addView(authorInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Quote")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val text = quoteInput.text.toString().trim()
                val author = authorInput.text.toString().trim().ifEmpty { "Unknown" }
                if (text.isNotEmpty()) {
                    customQuoteManager.updateQuote(quote.id, text, author)
                    loadQuotes()
                    Toast.makeText(this, "Quote updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddQuoteDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)

        val quoteInput = EditText(this)
        quoteInput.hint = "Enter your quote"
        layout.addView(quoteInput)

        val authorInput = EditText(this)
        authorInput.hint = "Author name (optional)"
        layout.addView(authorInput)

        AlertDialog.Builder(this)
            .setTitle("Add Your Quote")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val text = quoteInput.text.toString().trim()
                val author = authorInput.text.toString().trim().ifEmpty { "Unknown" }

                if (text.isNotEmpty()) {
                    customQuoteManager.addQuote(text, author, "My Quotes")
                    loadQuotes()
                    Toast.makeText(this, "Quote added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a quote", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}