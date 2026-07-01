package com.example.quoteapp.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.quoteapp.R
import com.example.quoteapp.data.CategoryTheme
import com.example.quoteapp.data.FavoritesManager
import com.example.quoteapp.data.QuoteData
import com.example.quoteapp.model.Quote
import java.io.File
import java.io.FileOutputStream
import com.example.quoteapp.data.SharedQuoteManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class QuoteDetailActivity : AppCompatActivity() {

    private lateinit var favoritesManager: FavoritesManager
    private lateinit var quote: Quote
    private lateinit var sharedQuoteManager: SharedQuoteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote_detail)

        favoritesManager = FavoritesManager(this)
        sharedQuoteManager = SharedQuoteManager(this)

        val isCustom = intent.getBooleanExtra("isCustom", false)

        quote = if (isCustom) {
            val text = intent.getStringExtra("customText") ?: ""
            val author = intent.getStringExtra("customAuthor") ?: "Unknown"
            val cat = intent.getStringExtra("customCategory") ?: "Motivation"
            Quote(0, text, author, cat, isCustom = true)
        } else {
            val quoteId = intent.getIntExtra("quoteId", -1)
            QuoteData.quotes.find { it.id == quoteId } ?: return
        }

        findViewById<TextView>(R.id.tvDetailQuote).text = "❝ ${quote.text} ❞"
        findViewById<TextView>(R.id.tvDetailAuthor).text = "— ${quote.author}"

        val ivBackground = findViewById<ImageView>(R.id.ivDetailBackground)
        if (CategoryTheme.usesImage(quote.category)) {
            ivBackground.setImageResource(CategoryTheme.getImageBackground(quote.category))
        } else {
            ivBackground.setImageResource(CategoryTheme.getGradientBackground(quote.category))
        }

        val btnFavorite = findViewById<ImageButton>(R.id.btnFavoriteDetail)
        updateFavoriteIcon(btnFavorite)

        btnFavorite.setOnClickListener {
            if (favoritesManager.isFavorite(quote)) {
                favoritesManager.removeFavorite(quote)
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            } else {
                favoritesManager.saveFavorite(quote)
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
            updateFavoriteIcon(btnFavorite)
        }
        findViewById<ImageButton>(R.id.btnShareDetail).setOnClickListener {
            showShareOptionsDialog()
        }

        findViewById<ImageButton>(R.id.btnBackDetail).setOnClickListener {
            finish()
        }
    }

    private fun updateFavoriteIcon(btn: ImageButton) {
        val isFav = favoritesManager.isFavorite(quote)
        btn.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    private fun shareQuoteAsImage() {
        try {
            sharedQuoteManager.addSharedQuote(quote)
            val captureArea = findViewById<RelativeLayout>(R.id.captureArea)

            val bitmap = Bitmap.createBitmap(
                captureArea.width,
                captureArea.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            captureArea.draw(canvas)

            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "quote_share.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val uri = FileProvider.getUriForFile(
                this,
                "com.example.quoteapp.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/png"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share Quote"))

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showShareOptionsDialog() {
        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_share, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<TextView>(R.id.optionShareText).setOnClickListener {
            dialog.dismiss()
            shareQuoteAsText()
        }

        sheetView.findViewById<TextView>(R.id.optionShareImage).setOnClickListener {
            dialog.dismiss()
            shareQuoteAsImage()
        }

        sheetView.findViewById<TextView>(R.id.optionCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun shareQuoteAsText() {
        sharedQuoteManager.addSharedQuote(quote)
        val shareText = "❝ ${quote.text} ❞\n— ${quote.author}"
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    }
}