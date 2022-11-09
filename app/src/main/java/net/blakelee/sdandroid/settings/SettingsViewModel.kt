package net.blakelee.sdandroid.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import net.blakelee.sdandroid.network.StableDiffusionRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: StableDiffusionRepository,
    private val appState: AppState
) : ViewModel() {

    val url: String by appState::url

    var sampler: String by appState::sampler
    var samplers: List<String> by mutableStateOf(listOf("Euler a"))

    val model: String by appState::model
    var models: List<String> by mutableStateOf(listOf())

    init {
        viewModelScope.launch {
            runCatching {
                samplers = repository.samplers()

                models = repository.models()

                appState.model = repository.options().sd_model_checkpoint
            }
        }
    }

    fun setModel(model: String) {
        viewModelScope.launch {
            runCatching {
                repository.model(model)
                appState.model = model
            }.onFailure {
                Log.d(this::class.simpleName.orEmpty(), "Failed to set model: $it")
            }
        }
    }
}