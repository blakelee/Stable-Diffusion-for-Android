package net.blakelee.sdandroid.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.persistence.Config
import javax.inject.Inject

@HiltViewModel
class LandingPageViewModel @Inject constructor(
    private val config: Config,
    repository: LoginRepository
) : ViewModel() {

    val isLoggedIn = config.urlFlow.map { it.isNotBlank() }

    fun login(url: String) {
        viewModelScope.launch {
            config.setUrl(url)
        }
    }
}