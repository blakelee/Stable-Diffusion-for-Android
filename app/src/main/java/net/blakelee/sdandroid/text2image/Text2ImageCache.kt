package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.network.StableDiffusionRepository
import net.blakelee.sdandroid.persistence.SharedCache
import net.blakelee.sdandroid.persistence.SharedPreferencesKeys
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Text2ImageCache @Inject constructor(
    private val repository: StableDiffusionRepository,
    override val dataStore: DataStore<Preferences>
) : SharedCache(Text2ImageCache) {

    private val workingPrompt = MutableStateFlow<String?>(null)

    val prompt: Flow<String> = prompts.combine(workingPrompt) { prompts, workingPrompt ->
        val prompt = prompts.lastOrNull().orEmpty()
        workingPrompt ?: prompt
    }

    suspend fun setPrompt(prompt: String) {
        workingPrompt.emit(prompt)
    }

    val images = MutableStateFlow(emptyList<Bitmap>())

    suspend fun submit(
        sampler: String,
        cfg: Float,
        steps: Int,
        width: Int,
        height: Int,
        batchCount: Int,
        batchSize: Int
    ): Flow<Boolean> = flow {
        runCatching {
            emit(true)

            val prompt = prompt.first()
            addPrompt(prompt)

            val response = repository.text2Image(
                prompt,
                cfg,
                steps,
                sampler,
                width,
                height,
                batchCount,
                batchSize
            )

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

    companion object : SharedPreferencesKeys {
        override val promptKey: String = "t2i_prompt_key"
    }
}