package com.calcnova.app.core

import android.content.Context
import com.calcnova.app.api.SettingsData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Stores app settings (theme, auto-calculate, haptics, angle mode) as a
 * single JSON object file in app-private storage. No DataStore, no Room,
 * no SharedPreferences-as-a-database pattern - just one small JSON file.
 */
class JsonSettingsStore(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val file: File
        get() = File(context.filesDir, "settings.json")

    fun load(): SettingsData {
        if (!file.exists()) return SettingsData()
        return try {
            val text = file.readText()
            if (text.isBlank()) SettingsData() else json.decodeFromString(text)
        } catch (e: Exception) {
            SettingsData()
        }
    }

    fun save(settings: SettingsData) {
        file.writeText(json.encodeToString(settings))
    }
}
