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

    val url = dataStore.url

    val isLoggedIn = url.map { url -> url.isNotBlank() }

    suspend fun login(url: String, username: String, password: String): String? {
        if (url.isBlank()) return "URL is blank"

        val url = when (url.any { !it.isLetterOrDigit() }) {
            true -> url.dropLastWhile { it == '/' }
            false -> "https://$url.gradio.app"
        }

        val result = service.isLoggedIn("$url/login_check")
        val isUnauthorized = result.code() == 401
        when {
            result.isSuccessful -> {
                dataStore.setUrl(url)
                return null
            }
            !result.isSuccessful && !isUnauthorized -> return null
            isUnauthorized -> {
                if (username.isNotBlank()) {
                    val loginResult = loginResult(username, password, url)

                    when {
                        loginResult.isSuccess -> dataStore.setUrl(url)
                        loginResult.isFailure -> return result.message()
                    }
                } else if (result.body() != null) {
                    dataStore.setUrl(url)
                }
            }
        }

        return null
    }

    private suspend fun loginResult(username: String, password: String, url: String) = runCatching {
        service.login(username, password, "$url/login")
    }

    fun logout() {
        dataStore.setUrl("")
    }
}