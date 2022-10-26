package net.blakelee.sdandroid.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.persistence.Config
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    config: Config
) : ViewModel() {
    val isLoggedIn = config.url.isNotBlank()
}