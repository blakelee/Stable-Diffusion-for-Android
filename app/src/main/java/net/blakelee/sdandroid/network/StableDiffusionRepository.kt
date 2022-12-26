package net.blakelee.sdandroid.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StableDiffusionRepository @Inject constructor(
    private val service: StableDiffusionService
) {

    suspend fun text2Image(
        prompt: String,
        cfg: Float,
        steps: Int,
        sampler: String,
        width: Int,
        height: Int,
        batchCount: Int,
        batchSize: Int
    ): Text2ImageResponse {
        val body = Text2ImageBody(
            prompt = prompt,
            steps = steps,
            cfg_scale = cfg,
            sampler_name = sampler,
            width = width,
            height = height,
            n_iter = batchCount,
            batch_size = batchSize
        )
        return service.text2Image(body)
    }

    suspend fun image2Image(
        prompt: String,
        cfg: Float,
        steps: Int,
        sampler: String,
        denoisingStrength: Float,
        base64Bitmap: String,
        width: Int,
        height: Int,
        batchCount: Int,
        batchSize: Int
    ): Image2ImageResponse {
        val body = Image2ImageBody(
            prompt = prompt,
            steps = steps,
            cfg_scale = cfg,
            sampler_name = sampler,
            init_images = listOf(base64Bitmap),
            denoising_strength = denoisingStrength,
            include_init_images = true,
            width = width,
            height = height,
            n_iter = batchCount,
            batch_size = batchSize
        )

        return service.image2Image(body)
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