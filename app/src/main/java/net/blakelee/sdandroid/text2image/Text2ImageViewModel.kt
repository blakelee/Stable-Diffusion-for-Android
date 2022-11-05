package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.di.AppState
import net.blakelee.sdandroid.network.StableDiffusionService
import net.blakelee.sdandroid.network.Text2ImageBody
import net.blakelee.sdandroid.persistence.Config
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class Text2ImageViewModel @Inject constructor(
    private val service: StableDiffusionService,
    private val appConfig: Config,
    appState: AppState
) : ViewModel(), AppState by appState {

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

    data class State(
        val prompt: String = "",
        val configuration: Float = 7f,
        val steps: Int = 20,
        val images: List<Bitmap> = emptyList(),
        val url: Flow<String>
    )


    var state by mutableStateOf(State(url = appConfig.urlFlow))
        private set

    fun setPrompt(prompt: String) {
        state = state.copy(prompt = prompt)
    }

    fun setConfigurationScale(configuration: Float) {
        state = state.copy(configuration = configuration)
    }

    fun setSteps(steps: Int) {
        state = state.copy(steps = steps)
    }

    fun submit() {
        viewModelScope.launch {
            runCatching {
                processing = true

                progress()

                val body = with(state) { Text2ImageBody(prompt, steps, configuration) }
                val response = service.text2Image(body)

                state = state.copy(images = response.images.mapToBitmap())

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
            appConfig.setUrl("")
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