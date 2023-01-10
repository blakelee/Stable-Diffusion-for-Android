package net.blakelee.sdandroid.landing

import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.AppDataStore
import net.blakelee.sdandroid.network.StableDiffusionService
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val service: StableDiffusionService,
    private val dataStore: AppDataStore
) {

    val url = dataStore.url

    val isLoggedIn = url.map { url -> url.isNotBlank() }

    suspend fun login(url: String, username: String, password: String) {
        if (url.isBlank()) return

        val url = when (url.any { !it.isLetterOrDigit() }) {
            true -> url.dropLastWhile { it == '/' }
            false -> "https://$url.gradio.app"
        }

        val result = runCatching { service.isLoggedIn("$url/login_check") }
        val isUnauthorized = (result.exceptionOrNull() as? HttpException)?.code() == 401
        when {
            result.isFailure && !isUnauthorized -> return
            isUnauthorized -> {
                if (username.isNotBlank()) {
                    val loginResult = loginResult(username, password, url)

                    when {
                        loginResult.isSuccess -> dataStore.setUrl(url)
                        loginResult.isFailure -> return
                    }
                } else if (result.getOrNull() == true) {
                    dataStore.setUrl(url)
                }
            }
        }
    }

    private suspend fun loginResult(username: String, password: String, url: String) = runCatching {
        service.login(username, password, "$url/login")
    }

    fun logout() {
        dataStore.setUrl("")
    }
}