package net.blakelee.sdandroid.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import javax.inject.Inject

@HiltViewModel
class LandingPageViewModel @Inject constructor(
    appState: AppState
) : ViewModel(), AppState by appState {

    fun login(url: String) {
        val url = when (url.any { !it.isLetterOrDigit() }) {
            true -> url.dropLastWhile { it == '/' }
            false -> "https://$url.gradio.app"
        }

        viewModelScope.launch {
            this@LandingPageViewModel.url = url
        }
    }
}
