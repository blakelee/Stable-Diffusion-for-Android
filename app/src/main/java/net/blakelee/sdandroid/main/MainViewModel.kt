package net.blakelee.sdandroid.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.blakelee.sdandroid.di.AppState
import net.blakelee.sdandroid.persistence.Config
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    config: Config,
    appState: AppState
) : ViewModel(), AppState by appState {
    val isLoggedIn = config.url.isNotBlank()

    fun cancel() {
        onCancel()
    }

    fun submit() {
        onProcess()
    }

}