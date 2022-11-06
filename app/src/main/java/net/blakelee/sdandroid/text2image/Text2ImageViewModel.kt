package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import net.blakelee.sdandroid.network.StableDiffusionService
import net.blakelee.sdandroid.network.Text2ImageBody
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class Text2ImageViewModel @Inject constructor(
    private val service: StableDiffusionService,
    appState: AppState,
    config: Text2ImageConfig
) : ViewModel(), AppState by appState, Text2ImageConfig by config {

    init {
        onCancel = {
            viewModelScope.cancel()
            processing = false
            progress = 0f
        }

        onProcess = {
            submit()
        }
    }

    var prompt: TextFieldValue by mutableStateOf(TextFieldValue(prompts.firstOrNull() ?: ""))
    var images: List<Bitmap> by mutableStateOf(emptyList())

    fun setPrompt(prompt: String) {
        this.prompt = this.prompt.copy(text = prompt, selection = TextRange(prompt.length))
    }

    fun setConfigurationScale(configuration: Float) {
        cfgScale = configuration
    }

    fun deletePrompt(prompt: String) {
        val prompts = (prompts as LinkedHashSet)
        prompts.remove(prompt)
        this.prompts = prompts
    }

    fun selectAllText() {
        prompt = prompt.copy(
            selection = TextRange(0, prompt.text.length)
        )
    }

    private fun addPrompt(prompt: String) {
        prompts = prompts + prompt
    }

    fun submit() {
        viewModelScope.launch {
            runCatching {
                processing = true

                progress()

                addPrompt(prompt.text)

                val body = Text2ImageBody(prompt.text, steps, cfgScale)
                val response = service.text2Image(body)

                images = response.images.mapToBitmap()

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
                    val serverProgress = service.progress().progress
                    progress = if (serverProgress == 0f && hasLoaded) 1f else serverProgress
                    hasLoaded = true
                    this@Text2ImageViewModel.progress = progress
                    delay(250)
                } while (processing)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            url = ""
        }
    }

    fun interupt() {
        viewModelScope.launch {
            runCatching { service.interrupt() }
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