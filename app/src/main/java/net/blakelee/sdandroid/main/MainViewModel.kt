package net.blakelee.sdandroid.main

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    appState: AppState,

    ) : ViewModel(), AppState by appState {

    val isLoggedIn by derivedStateOf { url.isNotBlank() }

    fun cancel() {
        onCancel()
    }

    fun submit() {
        onProcess()
    }

    fun logout() {
        viewModelScope.launch { url = "" }
    }
}