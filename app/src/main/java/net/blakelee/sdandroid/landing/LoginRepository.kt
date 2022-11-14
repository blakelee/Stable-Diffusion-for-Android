package net.blakelee.sdandroid.landing

import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.AppDataStore
import net.blakelee.sdandroid.network.StableDiffusionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val service: StableDiffusionService,
    private val dataStore: AppDataStore
) {

    suspend fun isLoggedIn() = service.isLoggedIn()

    val url = dataStore.url

    val isLoggedIn = url.map { url -> url.isNotBlank() }

    fun login(url: String) {
        if (url.isBlank()) return

        val url = when (url.any { !it.isLetterOrDigit() }) {
            true -> url.dropLastWhile { it == '/' }
            false -> "https://$url.gradio.app"
        }

        dataStore.setUrl(url)
    }

    fun logout() {
        dataStore.setUrl("")
    }
}