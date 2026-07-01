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
import com.example.quoteapp.adapter.CategoryAdapter
import com.example.quoteapp.data.QuoteData
import com.example.quoteapp.ui.CategoryActivity

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        rvCategories.adapter = CategoryAdapter(QuoteData.getCategories()) { category ->
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            intent.putExtra("category", category)
            startActivity(intent)
        }
    }
}