package net.blakelee.sdandroid.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SharedPreferencesKeys {
    val promptKey: String
    val stepsKey: String
    val cfgScaleKey: String
}

abstract class SharedCache(val keys: SharedPreferencesKeys) {

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

    val steps
        get() = dataStore.data
            .map { preferences -> preferences[intPreferencesKey(keys.stepsKey)] ?: 20 }

    suspend fun setSteps(steps: Int) {
        dataStore.edit { settings -> settings[intPreferencesKey(keys.stepsKey)] = steps }
    }

    val cfgScale
        get() = dataStore.data
            .map { preferences -> preferences[floatPreferencesKey(keys.cfgScaleKey)] ?: 7f }

    suspend fun setCfgScale(cfgScale: Float) {
        dataStore.edit { settings -> settings[floatPreferencesKey(keys.cfgScaleKey)] = cfgScale }
    }
}