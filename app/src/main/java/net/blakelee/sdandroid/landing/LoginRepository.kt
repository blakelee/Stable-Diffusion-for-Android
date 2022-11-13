package net.blakelee.sdandroid.landing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.network.StableDiffusionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(private val service: StableDiffusionService) {

    suspend fun isLoggedIn() = service.isLoggedIn()

    private val _url: MutableStateFlow<String> = MutableStateFlow("")
    val url = _url.asSharedFlow()

    val isLoggedIn = _url.map { url ->
        url.isNotBlank()
    }

    fun login(url: String) {
        if (url.isBlank()) return

        val url = when (url.any { !it.isLetterOrDigit() }) {
            true -> url.dropLastWhile { it == '/' }
            false -> "https://$url.gradio.app"
        }

        _url.tryEmit(url)
    }
}