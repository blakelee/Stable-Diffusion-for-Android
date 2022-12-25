package net.blakelee.sdandroid.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SharedPreferencesKeys {
    val promptKey: String
}

abstract class SharedCache(private val keys: SharedPreferencesKeys) {

    protected abstract val dataStore: DataStore<Preferences>

    val prompts
        get() = dataStore.data
            .map { preferences ->
                LinkedHashSet<String>().apply {
                    preferences[stringSetPreferencesKey(keys.promptKey)]?.let(::addAll)
                }
            }

    suspend fun addPrompt(prompt: String) {
        prompts.map { prompts ->
            dataStore.edit { settings ->
                settings[stringSetPreferencesKey(keys.promptKey)] = prompts + prompt
            }
        }.first()
    }

    suspend fun deletePrompt(prompt: String) {
        prompts.map { prompts ->
            dataStore.edit { settings ->
                settings[stringSetPreferencesKey(keys.promptKey)] = prompts - prompt
            }
        }.first()
    }
}