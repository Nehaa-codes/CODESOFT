package com.example.quoteapp.data

import android.content.Context
import com.example.quoteapp.model.Quote
import org.json.JSONArray
import org.json.JSONObject

class SharedQuoteManager(context: Context) {

    private val prefs = context.getSharedPreferences("shared_quotes", Context.MODE_PRIVATE)

    fun addSharedQuote(quote: Quote) {
        val list = getSharedQuotes().toMutableList()
        // Avoid duplicates piling up at top - remove if already exists, then re-add at front
        list.removeAll { it.id == quote.id && it.text == quote.text }
        list.add(0, quote)
        // Keep only last 50
        val trimmed = if (list.size > 50) list.subList(0, 50) else list
        saveAll(trimmed)
    }

    fun getSharedQuotes(): List<Quote> {
        val json = prefs.getString("shared_json", null) ?: return emptyList()
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
                    isCustom = obj.optBoolean("isCustom", false)
                )
            )
        }
        return list
    }

    private fun saveAll(list: List<Quote>) {
        val array = JSONArray()
        for (q in list) {
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("text", q.text)
            obj.put("author", q.author)
            obj.put("category", q.category)
            obj.put("isCustom", q.isCustom)
            array.put(obj)
        }
        prefs.edit().putString("shared_json", array.toString()).apply()
    }
}