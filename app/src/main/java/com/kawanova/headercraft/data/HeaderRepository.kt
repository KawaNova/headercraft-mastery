package com.kawanova.headercraft.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "headercraft_prefs")

class HeaderRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val savedConfigsKey = stringPreferencesKey("saved_header_configs_json")

    val savedConfigs: Flow<List<SavedHeaderConfig>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[savedConfigsKey]
        if (rawJson.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<SavedHeaderConfig>>(rawJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveConfig(config: SavedHeaderConfig) {
        context.dataStore.edit { prefs ->
            val rawJson = prefs[savedConfigsKey]
            val currentList = if (rawJson.isNullOrBlank()) {
                mutableListOf()
            } else {
                try {
                    json.decodeFromString<List<SavedHeaderConfig>>(rawJson).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            }
            // If already exists, replace; else prepend
            val index = currentList.indexOfFirst { it.id == config.id }
            if (index >= 0) {
                currentList[index] = config
            } else {
                currentList.add(0, config)
            }
            prefs[savedConfigsKey] = json.encodeToString(currentList)
        }
    }

    suspend fun deleteConfig(id: String) {
        context.dataStore.edit { prefs ->
            val rawJson = prefs[savedConfigsKey]
            if (!rawJson.isNullOrBlank()) {
                try {
                    val currentList = json.decodeFromString<List<SavedHeaderConfig>>(rawJson).toMutableList()
                    currentList.removeAll { it.id == id }
                    prefs[savedConfigsKey] = json.encodeToString(currentList)
                } catch (_: Exception) {}
            }
        }
    }
}
