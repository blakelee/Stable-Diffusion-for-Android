package net.blakelee.sdandroid.img2img

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import net.blakelee.sdandroid.network.StableDiffusionRepository
import javax.inject.Inject

@HiltViewModel
class Image2ImageViewModel @Inject constructor(
    private val repository: StableDiffusionRepository,
    appState: AppState
) : ViewModel(), AppState by appState {

    fun init() {
        onCancel = ::interrupt
        onProcess = ::submit
    }

    fun submit() {

    }

    fun interrupt() {
        viewModelScope.launch {
            runCatching { repository.interrupt() }
        }
    }
}