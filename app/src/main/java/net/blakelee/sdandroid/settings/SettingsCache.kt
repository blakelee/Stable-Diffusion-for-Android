package net.blakelee.sdandroid.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.network.StableDiffusionRepository
import javax.inject.Inject
import javax.inject.Singleton

private val MODEL_KEY = stringPreferencesKey("model")
private val MODELS_KEY = stringSetPreferencesKey("models")
private val SAMPLER_KEY = stringPreferencesKey("sampler")
private val SAMPLERS_KEY = stringSetPreferencesKey("samplers")

@Singleton
class SettingsCache @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val repository: StableDiffusionRepository
) {

    val model: Flow<String> = flow {
        runCatching { emit(repository.options().sd_model_checkpoint) }
            .onFailure { emit("") }
    }.combine(
        dataStore.data.map { preferences ->
            preferences[MODEL_KEY] ?: ""
        }
    ) { server, dataStore -> server.ifEmpty { dataStore } }

    suspend fun setModel(model: String) {
        val success = runCatching { repository.model(model) }.isSuccess
        if (success) {
            dataStore.edit { preferences ->
                preferences[MODEL_KEY] = model
            }
        }
    }

    val models: Flow<Set<String>> = flow<Set<String>> {
        runCatching {
            val models = repository.models().toSortedSet()
            emit(models)
            dataStore.edit { preferences -> preferences[MODELS_KEY] = models }
        }.onFailure { emit(sortedSetOf()) }
    }.combine(dataStore.data.map { preferences ->
        preferences[MODELS_KEY] ?: sortedSetOf()
    }) { server, dataStore -> server.ifEmpty { dataStore } }

    val sampler: Flow<String> = dataStore.data.map { preferences ->
        preferences[SAMPLER_KEY] ?: "Euler a"
    }

    suspend fun setSampler(sampler: String) = dataStore.edit { preferences ->
        preferences[SAMPLER_KEY] = sampler
    }

    val samplers: Flow<Set<String>> = flow<Set<String>> {
        runCatching {
            val samplers = repository.samplers().toSortedSet()
            emit(samplers)
            dataStore.edit { preferences -> preferences[SAMPLERS_KEY] = samplers }
        }.onFailure { emit(sortedSetOf()) }
    }.combine(dataStore.data.map { preferences ->
        preferences[SAMPLERS_KEY] ?: sortedSetOf()
    }) { server, dataStore -> server.ifEmpty { dataStore } }
}