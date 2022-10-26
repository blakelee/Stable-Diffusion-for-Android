package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.network.StableDiffusionService
import net.blakelee.sdandroid.network.Text2ImageBody
import net.blakelee.sdandroid.persistence.Config
import javax.inject.Inject

@HiltViewModel
class Text2ImageViewModel @Inject constructor(
    private val service: StableDiffusionService,
    private val appConfig: Config
) : ViewModel() {

    var config by mutableStateOf(Text2ImageBody(""))
    var images by mutableStateOf<List<Bitmap>?>(null)

    fun setPrompt(prompt: String) {
        config = config.copy(prompt = prompt)
    }

    fun setConfigurationScale(configuration: Float) {
        config = config.copy(cfg_scale = configuration)
    }

    fun setSteps(steps: Int) {
        config = config.copy(steps = steps)
    }

    fun submit() {
        viewModelScope.launch {
            runCatching {
                val response = service.text2Image(config)

                images = response
                    .images
                    .map { encodedImage ->
                        val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            appConfig.setUrl("")
        }
    }
}