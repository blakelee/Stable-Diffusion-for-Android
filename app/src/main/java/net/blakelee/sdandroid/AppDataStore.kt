package net.blakelee.sdandroid

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val URL_KEY = stringPreferencesKey("url")

@Singleton
class AppDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val url: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[URL_KEY] ?: ""
        }

    fun setUrl(url: String) {
        runBlocking {
            dataStore.edit { settings ->
                settings[URL_KEY] = url
            }
        }
    }
}