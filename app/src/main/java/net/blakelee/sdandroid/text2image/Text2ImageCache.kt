package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.network.StableDiffusionRepository
import javax.inject.Inject
import javax.inject.Singleton

private val PROMPTS_KEY = stringSetPreferencesKey("prompts")
private val CFG_SCALE_KEY = floatPreferencesKey("cfg_scale")
private val STEPS_KEY = intPreferencesKey("steps")

@Singleton
class Text2ImageCache @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val repository: StableDiffusionRepository
) {

    val prompts: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            LinkedHashSet<String>().apply {
                preferences[PROMPTS_KEY]?.let(::addAll)
            }
        }

    private val workingPrompt = MutableStateFlow<String?>(null)

    val prompt: Flow<String> = prompts.combine(workingPrompt) { prompts, workingPrompt ->
        val prompt = prompts.firstOrNull().orEmpty()
        workingPrompt ?: prompt
    }

    suspend fun setPrompt(prompt: String) {
        workingPrompt.emit(prompt)
    }

    suspend fun deletePrompt(prompt: String) {
        val prompts = prompts.first()
        dataStore.edit { settings ->
            settings[PROMPTS_KEY] = prompts - prompt
        }
    }

    val steps: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[STEPS_KEY] ?: 20
        }

    val images = MutableStateFlow(emptyList<Bitmap>())

    val cfgScale: Flow<Float> =
        dataStore.data.map { preferences -> preferences[CFG_SCALE_KEY] ?: 7f }

    suspend fun setCfgScale(cfgScale: Float) =
        dataStore.edit { settings -> settings[CFG_SCALE_KEY] = cfgScale }

    private suspend fun addPrompt(prompt: String) {
        val prompts = prompts.first()
        dataStore.edit { settings ->
            settings[PROMPTS_KEY] = prompts + prompt
        }
    }

    suspend fun setSteps(steps: Int) = dataStore.edit { settings -> settings[STEPS_KEY] = steps }

    suspend fun submit(sampler: String): Flow<Boolean> = flow {
        runCatching {
            emit(true)

            val cfgScale = cfgScale.first()
            val steps = steps.first()

            val prompt = prompt.first()
            addPrompt(prompt)

            val response = repository.text2Image(prompt, cfgScale, steps, sampler)

            images.emit(response.images.mapToBitmap())
            // Artificial delay to finish the animation
            delay(350)

        }

        runCatching { emit(false) }
    }

    private fun List<String>.mapToBitmap(): List<Bitmap> {
        return this.map { encodedString ->
            val strippedEncodedImage = encodedString
                .replace("data:image/png;base64,", "")
                .replace("data:image/jpeg;base64,", "")

            val decodedString = Base64.decode(strippedEncodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }
}