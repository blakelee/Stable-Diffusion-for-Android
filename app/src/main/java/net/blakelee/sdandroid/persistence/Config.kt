package net.blakelee.sdandroid.persistence

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

private object PreferencesKeys {
    val URL = stringPreferencesKey("url")
}

class Config @Inject constructor(@ApplicationContext private val context: Context) {

    val url: String
        get() = runBlocking { context.dataStore.data.first()[PreferencesKeys.URL] ?: "" }

    val urlFlow: Flow<String> = context.dataStore
        .data
        .map { preferences ->
            preferences[PreferencesKeys.URL] ?: ""
        }

    suspend fun setUrl(url: String) {
        context.dataStore
            .edit { preferences ->
                preferences[PreferencesKeys.URL] = url
            }
    }
}