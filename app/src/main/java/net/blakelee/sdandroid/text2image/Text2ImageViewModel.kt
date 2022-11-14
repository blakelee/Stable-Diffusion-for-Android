package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import net.blakelee.sdandroid.network.StableDiffusionRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

//@HiltViewModel
@Singleton
class Text2ImageViewModel @Inject constructor(
    private val repository: StableDiffusionRepository,
    appState: AppState,
    config: Text2ImageConfig
) : ViewModel(), AppState by appState, Text2ImageConfig by config {

    var prompt: String = prompts.firstOrNull() ?: ""

    val images = MutableStateFlow(emptyList<Bitmap>())

    fun init() {
        onCancel = ::interrupt
        onProcess = ::submit
    }

    fun setConfigurationScale(configuration: Float) {
        cfgScale = configuration
    }

    fun deletePrompt(prompt: String) {
        val prompts = (prompts as LinkedHashSet)
        prompts.remove(prompt)
        this.prompts = prompts
    }

    private fun addPrompt(prompt: String) {
        prompts = prompts + prompt
    }

    fun submit() {
        viewModelScope.launch {
            runCatching {
                processing = true

                progress()

                addPrompt("test prompt")

                val response = repository.text2Image("test prompt", cfgScale, steps)
                images.emit(response.images.mapToBitmap())

                // Artificial delay to finish the animation
                delay(350)
            }.onFailure {
                Log.d(this::class.simpleName, (it as? HttpException)?.message().orEmpty())
            }

            processing = false
            progress = 0f
        }
    }

    private fun progress() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                var progress: Float
                var hasLoaded = false
                do {
                    val serverProgress = repository.progress().progress
                    progress = if (serverProgress == 0f && hasLoaded) 1f else serverProgress
                    hasLoaded = true
                    this@Text2ImageViewModel.progress = progress
                    delay(250)
                } while (processing)
            }
        }
    }

    fun interrupt() {
        viewModelScope.launch {
            runCatching { repository.interrupt() }
        }
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