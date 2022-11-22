package net.blakelee.sdandroid.network

import kotlinx.coroutines.flow.firstOrNull
import net.blakelee.sdandroid.settings.SettingsCache
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class StableDiffusionRepository @Inject constructor(
    private val service: StableDiffusionService,
    private val settingsCache: Provider<SettingsCache>
) {

    suspend fun text2Image(prompt: String, cfg: Float, steps: Int): Text2ImageResponse {
        val sampler = settingsCache.get().sampler.firstOrNull() ?: "Euler a"
        val body = Text2ImageBody(prompt, steps, cfg, sampler)
        return service.text2Image(body)
    }

    suspend fun progress() = service.progress()

    suspend fun interrupt() = service.interrupt()

    suspend fun options() = service.options()

    suspend fun options(model: String) {
        val body = mapOf<String, Any>(
            "sd_model_checkpoint" to model
        )
        return service.options(body)
    }

    suspend fun models() = service.models().map { it.title }

    suspend fun model(model: String) = service.model(model)

    suspend fun samplers() = service.samplers().map { it.name }
}