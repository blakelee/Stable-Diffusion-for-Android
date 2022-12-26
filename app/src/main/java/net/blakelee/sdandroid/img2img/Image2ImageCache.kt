package net.blakelee.sdandroid.img2img

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.network.StableDiffusionRepository
import net.blakelee.sdandroid.persistence.SharedCache
import net.blakelee.sdandroid.persistence.SharedPreferencesKeys
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val denoisingStrengthKey = "i2i_denoising_strength_key"


@Singleton
class Image2ImageCache @Inject constructor(
    private val repository: StableDiffusionRepository,
    override val dataStore: DataStore<Preferences>
) : SharedCache(Image2ImageCache) {

    private val workingPrompt = MutableStateFlow<String?>(null)

    val prompt: Flow<String> = prompts.combine(workingPrompt) { prompts, workingPrompt ->
        val prompt = prompts.firstOrNull().orEmpty()
        workingPrompt ?: prompt
    }

    suspend fun setPrompt(prompt: String) {
        workingPrompt.emit(prompt)
    }

    val selectedImage = MutableStateFlow<Bitmap?>(null)

    suspend fun setSelectedImage(bitmap: Bitmap?) {
        selectedImage.emit(bitmap)
    }

    val images = MutableStateFlow(emptyList<Bitmap>())

    val denoisingStrength: Flow<Float> = dataStore.data.map { preferences ->
        preferences[floatPreferencesKey(denoisingStrengthKey)] ?: 0.75f
    }

    suspend fun setDenoisingStrength(denoisingStrength: Float) = dataStore.edit { preferences ->
        preferences[floatPreferencesKey(denoisingStrengthKey)] = denoisingStrength
    }

    suspend fun submit(
        sampler: String,
        cfg: Float,
        steps: Int,
        width: Int,
        height: Int,
        batchCount: Int,
        batchSize: Int
    ) = flow {
        runCatching {
            emit(true)

            val prompt = workingPrompt.value ?: return@runCatching
            addPrompt(prompt)

            val denoisingStrength = denoisingStrength.first()

            // Will cause crash
            val bitmap = selectedImage.first()!!

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            val base64BitmapString =
                "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)

            val response = repository.image2Image(
                prompt,
                cfg,
                steps,
                sampler,
                denoisingStrength,
                base64BitmapString,
                width,
                height,
                batchCount,
                batchSize
            )

            images.emit(response.images.mapToBitmap())
            // Artificial delay to finish the animation
            delay(350)
        }.onFailure {
            Log.e("Image2Image", it.message.orEmpty())
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
        override val promptKey: String = "i2i_prompt_key"
    }
}