package net.blakelee.sdandroid.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StableDiffusionRepository @Inject constructor(
    private val service: StableDiffusionService
) {

    suspend fun text2Image(
        prompt: String, cfg: Float, steps: Int, sampler: String
    ): Text2ImageResponse {
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

    suspend fun model(model: String) = service.options(mapOf("sd_model_checkpoint" to model))

    suspend fun samplers() = service.samplers().map { it.name }
}