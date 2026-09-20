package com.calcnova.app.core

import android.content.Context
import com.calcnova.app.api.HistoryEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Stores calculation history as a single JSON array file in app-private
 * storage. This is intentionally NOT a database - no Room, no SQLite,
 * no schema/migrations. Just read-the-file, decode JSON, mutate the list,
 * encode JSON, write-the-file.
 *
 * Fine for a few hundred/thousand history entries. If history ever needs
 * to scale far beyond that, that's the point to revisit this approach -
 * but for a calculator app's history list, a JSON file is enough.
 */
class JsonHistoryStore(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val file: File
        get() = File(context.filesDir, "history.json")

    fun loadAll(): List<HistoryEntry> {
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            if (text.isBlank()) emptyList() else json.decodeFromString(text)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun append(entry: HistoryEntry) {
        val current = loadAll().toMutableList()
        current.add(0, entry) // newest first
        saveAll(current)
    }

    fun deleteAt(index: Int) {
        val current = loadAll().toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            saveAll(current)
        }
    }

    fun clearAll() {
        saveAll(emptyList())
    }

    private fun saveAll(entries: List<HistoryEntry>) {
        file.writeText(json.encodeToString(entries))
    }
}
