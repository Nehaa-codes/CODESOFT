package com.example.quoteapp.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.quoteapp.R
import com.example.quoteapp.data.CategoryTheme
import com.example.quoteapp.data.FavoritesManager
import com.example.quoteapp.data.QuoteData
import com.example.quoteapp.model.Quote
import java.io.File
import java.io.FileOutputStream
import com.example.quoteapp.data.SharedQuoteManager
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeFragment : Fragment() {

    private lateinit var favoritesManager: FavoritesManager
    private lateinit var randomQuote: Quote
    private lateinit var sharedQuoteManager: SharedQuoteManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesManager = FavoritesManager(requireContext())
        sharedQuoteManager = SharedQuoteManager(requireContext())

        randomQuote = QuoteData.getQuoteOfTheDay()
        view.findViewById<TextView>(R.id.tvRandomQuote).text = "❝ ${randomQuote.text} ❞"
        view.findViewById<TextView>(R.id.tvRandomAuthor).text = "— ${randomQuote.author}"
        view.findViewById<TextView>(R.id.tvCategoryLabel).text = randomQuote.category

        val ivBackground = view.findViewById<ImageView>(R.id.ivBackground)
        if (CategoryTheme.usesImage(randomQuote.category)) {
            ivBackground.setImageResource(CategoryTheme.getImageBackground(randomQuote.category))
        } else {
            ivBackground.setImageResource(CategoryTheme.getGradientBackground(randomQuote.category))
        }

        val btnFavoriteQOTD = view.findViewById<ImageButton>(R.id.btnFavoriteQOTD)
        updateQOTDFavoriteIcon(btnFavoriteQOTD)
        btnFavoriteQOTD.setOnClickListener {
            if (favoritesManager.isFavorite(randomQuote)) {
                favoritesManager.removeFavorite(randomQuote)
                Toast.makeText(requireContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show()
            } else {
                favoritesManager.saveFavorite(randomQuote)
                Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
            updateQOTDFavoriteIcon(btnFavoriteQOTD)
        }

        view.findViewById<ImageButton>(R.id.btnShareQOTD).setOnClickListener {
            showShareOptionsDialog(view)
        }
        view.findViewById<ImageButton>(R.id.btnRefreshQuote).setOnClickListener {
            showNewRandomQuote(view)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<ImageButton>(R.id.btnFavoriteQOTD)?.let {
            updateQOTDFavoriteIcon(it)
        }
    }
    private fun showNewRandomQuote(view: View) {
        randomQuote = QuoteData.getRandom()
        view.findViewById<TextView>(R.id.tvRandomQuote).text = "❝ ${randomQuote.text} ❞"
        view.findViewById<TextView>(R.id.tvRandomAuthor).text = "— ${randomQuote.author}"
        view.findViewById<TextView>(R.id.tvCategoryLabel).text = randomQuote.category

        val ivBackground = view.findViewById<ImageView>(R.id.ivBackground)
        if (CategoryTheme.usesImage(randomQuote.category)) {
            ivBackground.setImageResource(CategoryTheme.getImageBackground(randomQuote.category))
        } else {
            ivBackground.setImageResource(CategoryTheme.getGradientBackground(randomQuote.category))
        }

        val btnFavoriteQOTD = view.findViewById<ImageButton>(R.id.btnFavoriteQOTD)
        updateQOTDFavoriteIcon(btnFavoriteQOTD)
    }

    private fun updateQOTDFavoriteIcon(btn: ImageButton) {
        val isFav = favoritesManager.isFavorite(randomQuote)
        btn.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    private fun showShareOptionsDialog(view: View) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_share, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<TextView>(R.id.optionShareText).setOnClickListener {
            dialog.dismiss()
            shareQuoteAsText()
        }

        sheetView.findViewById<TextView>(R.id.optionShareImage).setOnClickListener {
            dialog.dismiss()
            shareQuoteAsImage(view)
        }

        sheetView.findViewById<TextView>(R.id.optionCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun shareQuoteAsText() {
        sharedQuoteManager.addSharedQuote(randomQuote)
        val shareText = "❝ ${randomQuote.text} ❞\n— ${randomQuote.author}"
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    }

    private fun shareQuoteAsImage(view: View) {
        try {
            sharedQuoteManager.addSharedQuote(randomQuote)
            val captureArea = view.findViewById<ImageView>(R.id.ivBackground).parent as View

            val bitmap = Bitmap.createBitmap(
                captureArea.width,
                captureArea.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            captureArea.draw(canvas)

            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "quote_share.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.quoteapp.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/png"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share Quote"))

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}