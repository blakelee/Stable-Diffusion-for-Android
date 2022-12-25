package net.blakelee.sdandroid.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
private val CFG_KEY = floatPreferencesKey("cfg")
private val STEPS_KEY = intPreferencesKey("steps")
private val HEIGHT_KEY = intPreferencesKey("height")
private val WIDTH_KEY = intPreferencesKey("width")

data class SharedSettings(
    val sampler: String,
    val cfg: Float,
    val steps: Int,
    val width: Int,
    val height: Int
)

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

    val sampler: Flow<String> = get(SAMPLER_KEY, "Euler")

    suspend fun setSampler(sampler: String) = set(SAMPLER_KEY, sampler)

    val samplers: Flow<Set<String>> = flow<Set<String>> {
        runCatching {
            val samplers = repository.samplers().toSortedSet()
            emit(samplers)
            dataStore.edit { preferences -> preferences[SAMPLERS_KEY] = samplers }
        }.onFailure { emit(sortedSetOf()) }
    }.combine(dataStore.data.map { preferences ->
        preferences[SAMPLERS_KEY] ?: sortedSetOf()
    }) { server, dataStore -> server.ifEmpty { dataStore } }

    val steps: Flow<Int> = get(STEPS_KEY, 25)
    suspend fun setSteps(steps: Int) = set(STEPS_KEY, steps)

    val cfg: Flow<Float> = get(CFG_KEY, 8.5f)
    suspend fun setCfg(cfg: Float) = set(CFG_KEY, cfg)

    val height: Flow<Int> = get(HEIGHT_KEY, 512)
    suspend fun setHeight(height: Int) = set(HEIGHT_KEY, height)

    val width: Flow<Int> = get(WIDTH_KEY, 512)
    suspend fun setWidth(width: Int) = set(WIDTH_KEY, width)

    private suspend fun <T : Any> set(key: Preferences.Key<T>, value: T) =
        dataStore.edit { preferences ->
            preferences[key] = value
        }

    private fun <T : Any> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
}