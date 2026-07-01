package com.example.quoteapp.data

import android.content.Context
import com.example.quoteapp.model.Quote
import org.json.JSONArray
import org.json.JSONObject

class CustomQuoteManager(context: Context) {

    private val prefs = context.getSharedPreferences("custom_quotes", Context.MODE_PRIVATE)

    fun addQuote(text: String, author: String, category: String) {
        val list = getCustomQuotes().toMutableList()
        val newId = if (list.isEmpty()) 1001 else (list.maxOf { it.id } + 1)
        list.add(Quote(newId, text, author, category, isCustom = true))
        saveAll(list)
    }

    fun updateQuote(id: Int, newText: String, newAuthor: String) {
        val list = getCustomQuotes().toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = list[index]
            list[index] = old.copy(text = newText, author = newAuthor)
            saveAll(list)
        }
    }

    fun deleteQuote(id: Int) {
        val list = getCustomQuotes().toMutableList()
        list.removeAll { it.id == id }
        saveAll(list)
    }

    fun getCustomQuotes(): List<Quote> {
        val json = prefs.getString("quotes_json", null) ?: return emptyList()
        val array = JSONArray(json)
        val list = mutableListOf<Quote>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Quote(
                    id = obj.getInt("id"),
                    text = obj.getString("text"),
                    author = obj.getString("author"),
                    category = obj.getString("category"),
                    isCustom = true
                )
            )
        }
        return list
    }

    fun getCustomQuotesByCategory(category: String): List<Quote> {
        return getCustomQuotes().filter { it.category == category }
    }

    private fun saveAll(list: List<Quote>) {
        val array = JSONArray()
        for (q in list) {
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("text", q.text)
            obj.put("author", q.author)
            obj.put("category", q.category)
            array.put(obj)
        }
        prefs.edit().putString("quotes_json", array.toString()).apply()
    }
}