package net.blakelee.sdandroid.settings

import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.network.StableDiffusionService
import javax.inject.Inject
import javax.inject.Singleton

private val PROMPT_KEY = stringSetPreferencesKey("prompts")
private val MODEL_KEY = stringPreferencesKey("model")
private val MODELS_KEY = stringSetPreferencesKey("models")
private val SAMPLER_KEY = stringPreferencesKey("sampler")
private val SAMPLERS_KEY = stringSetPreferencesKey("samplers")
private val CFG_KEY = floatPreferencesKey("cfg")
private val STEPS_KEY = intPreferencesKey("steps")
private val RESTORE_FACES_KEY = booleanPreferencesKey("restoreFaces")
private val HEIGHT_KEY = intPreferencesKey("height")
private val WIDTH_KEY = intPreferencesKey("width")
private val COUNT_KEY = intPreferencesKey("batchCount")
private val SIZE_KEY = intPreferencesKey("sizeCount")
private val DENOISING_STRENGTH_KEY = floatPreferencesKey("denoisingStrength")

@Singleton
class SettingsCache @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val service: StableDiffusionService
) {

    val prompts
        get() = dataStore.data
            .map { preferences ->
                LinkedHashSet<String>().apply {
                    preferences[PROMPT_KEY]?.let(::addAll)
                }
            }

    suspend fun addPrompt(prompt: String) {
        prompts.map { prompts ->
            dataStore.edit { settings ->
                settings[PROMPT_KEY] = prompts + prompt
            }
        }.first()
    }

    suspend fun deletePrompt(prompt: String) {
        prompts.map { prompts ->
            dataStore.edit { settings ->
                settings[PROMPT_KEY] = prompts - prompt
            }
        }.first()
    }

    private val workingPrompt = MutableStateFlow<String?>(null)

    val prompt: Flow<String> = prompts.combine(workingPrompt) { prompts, workingPrompt ->
        val prompt = prompts.lastOrNull().orEmpty()
        workingPrompt ?: prompt
    }

    suspend fun setPrompt(prompt: String) {
        workingPrompt.emit(prompt)
    }

    val images = MutableStateFlow(emptyList<Bitmap>())

    val selectedImage = MutableStateFlow<Bitmap?>(null)

    suspend fun setSelectedImage(bitmap: Bitmap?) {
        selectedImage.emit(bitmap)
    }

    val model: Flow<String> = flow {
        runCatching { emit(service.options().sd_model_checkpoint) }
            .onFailure { emit("") }
    }.combine(
        dataStore.data.map { preferences ->
            preferences[MODEL_KEY] ?: ""
        }
    ) { server, dataStore -> server.ifEmpty { dataStore } }

    suspend fun setModel(model: String) {
        val body = mapOf("sd_model_checkpoint" to model)
        val success = runCatching { service.options(body) }.isSuccess
        if (success) {
            dataStore.edit { preferences ->
                preferences[MODEL_KEY] = model
            }
        }
    }

    val models: Flow<Set<String>> = flow<Set<String>> {
        runCatching {
            val models = service.models().map { it.title }.toSortedSet()
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
            val samplers = service.samplers().map { it.name }.toSortedSet()
            emit(samplers)
            dataStore.edit { preferences -> preferences[SAMPLERS_KEY] = samplers }
        }.onFailure { emit(sortedSetOf()) }
    }.combine(dataStore.data.map { preferences ->
        preferences[SAMPLERS_KEY] ?: sortedSetOf()
    }) { server, dataStore -> server.ifEmpty { dataStore } }

    val steps: Flow<Int> = get(STEPS_KEY, 25)
    suspend fun setSteps(steps: Int) = set(STEPS_KEY, steps)

    val restoreFaces: Flow<Boolean> = get(RESTORE_FACES_KEY, false)
    suspend fun setRestoreFaces(restoreFaces: Boolean) = set(RESTORE_FACES_KEY, restoreFaces)

    val cfg: Flow<Float> = get(CFG_KEY, 8.5f)
    suspend fun setCfg(cfg: Float) = set(CFG_KEY, cfg)

    val denoisingStrength = get(DENOISING_STRENGTH_KEY, 0.5f).map { (it * 100).toInt() }
    suspend fun setDenoisingStrength(denoisingStrength: Int) =
        set(DENOISING_STRENGTH_KEY, denoisingStrength / 100f)

    val height: Flow<Int> = get(HEIGHT_KEY, 512)
    suspend fun setHeight(height: Int) = set(HEIGHT_KEY, height)

    val width: Flow<Int> = get(WIDTH_KEY, 512)
    suspend fun setWidth(width: Int) = set(WIDTH_KEY, width)

    val batchCount: Flow<Int> = get(COUNT_KEY, 1)
    suspend fun setBatchCount(batchCount: Int) = set(COUNT_KEY, batchCount)

    val batchSize: Flow<Int> = get(SIZE_KEY, 1)
    suspend fun setBatchSize(batchSize: Int) = set(SIZE_KEY, batchSize)

    private suspend fun <T : Any> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private fun <T : Any> get(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
}