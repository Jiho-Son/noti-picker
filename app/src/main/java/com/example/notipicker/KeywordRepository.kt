package com.example.notipicker

import android.content.Context
import androidx.core.content.edit

class KeywordRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getKeywords(): List<String> {
        val raw = prefs.getString(KEY_KEYWORDS, "") ?: ""
        return raw.split(DELIMITER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun addKeyword(keyword: String) {
        val normalized = keyword.trim()
        if (normalized.isEmpty()) return
        val current = getKeywords().toMutableSet()
        current.add(normalized)
        save(current.toList())
    }

    fun removeKeyword(keyword: String) {
        val current = getKeywords().toMutableList()
        current.remove(keyword)
        save(current)
    }

    private fun save(list: List<String>) {
        val raw = list.joinToString(DELIMITER)
        prefs.edit {
            putString(KEY_KEYWORDS, raw)
        }
    }

    companion object {
        private const val PREF_NAME = "keyword_prefs"
        private const val KEY_KEYWORDS = "keywords"
        private const val DELIMITER = ";;"
    }
}

