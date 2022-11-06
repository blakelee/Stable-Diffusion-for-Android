package net.blakelee.sdandroid.landing

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import javax.inject.Inject

@HiltViewModel
class LandingPageViewModel @Inject constructor(
    repository: LoginRepository,
    appState: AppState
) : ViewModel(), AppState by appState {

    val isLoggedIn by derivedStateOf { url.isNotBlank() }

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
