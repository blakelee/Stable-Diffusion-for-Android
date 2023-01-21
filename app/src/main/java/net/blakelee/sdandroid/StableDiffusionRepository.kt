package net.blakelee.sdandroid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.network.StableDiffusionService
import net.blakelee.sdandroid.settings.SettingsCache
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class StableDiffusionRepository @Inject constructor(
    private val settingsCache: SettingsCache,
    private val service: StableDiffusionService,
) {

    fun submit() = channelFlow {
        send(0f)

        val prompt = settingsCache.prompt.first()
        settingsCache.addPrompt(prompt)

        var submit = service::text2Image

        val body = mutableMapOf<String, Any>(
            "prompt" to prompt,
            "steps" to settingsCache.steps.first(),
            "cfg_scale" to settingsCache.cfg.first(),
            "sampler_name" to settingsCache.sampler.first(),
            "width" to settingsCache.width.first(),
            "height" to settingsCache.height.first(),
            "n_iter" to settingsCache.batchCount.first(),
            "batch_size" to settingsCache.batchSize.first()
        )

        settingsCache.selectedImage.first()?.let { selectedImage ->
            body += mapOf(
                "init_images" to listOf(selectedImage.asString),
                "denoising_strength" to settingsCache.denoisingStrength.first(),
                "include_init_images" to true
            )

            submit = service::image2Image
        }

        coroutineScope {
            val progress = async(Dispatchers.IO) {
                progressFlow().collectLatest {
                    send(it)
                }
            }

            withContext(Dispatchers.IO) {
                runCatching {
                    val response = submit(body)
                    settingsCache.images.emit(response.images.map { it.asBitmap })
                }
            }

            progress.cancel()

        }

        send(1f)
    }

    private val Bitmap.asString: String
        get() {
            val byteArrayOutputStream = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

    private val String.asBitmap: Bitmap
        get() {
            val strippedEncodedImage = replace("data:image/png;base64,", "")
                .replace("data:image/jpeg;base64,", "")

            val decodedString = Base64.decode(strippedEncodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }

    private fun progressFlow(): Flow<Float> = flow {
        runCatching {
            emit(0f)

            var progress = 0f
            do {
                delay(200)
                val newProgress =
                    runCatching { service.progress().progress }.getOrNull() ?: 0f
                emit(newProgress)
                progress = maxOf(newProgress, progress)

            } while (newProgress >= progress)

            emit(1f)
        }
    }

    suspend fun progress() = service.progress()

    suspend fun interrupt() = service.interrupt()

    suspend fun options() = service.options()

    suspend fun options(model: String) {
        val body = mapOf<String, Any>(
            "sd_model_checkpoint" to model
        )
        return service.options(body)
    }

    suspend fun models() = service.models().map { it.title }

    suspend fun model(model: String) = service.options(mapOf("sd_model_checkpoint" to model))

    suspend fun samplers() = service.samplers().map { it.name }
}