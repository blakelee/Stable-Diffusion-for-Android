package net.blakelee.sdandroid.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StableDiffusionService {

    @GET("login_check")
    suspend fun isLoggedIn(): Boolean

    @GET("sdapi/v1/progress")
    suspend fun progress(): ProgressBody

    @POST("sdapi/v1/interrupt")
    suspend fun interrupt()

    @GET("sdapi/v1/samplers")
    suspend fun samplers(): List<SamplersBody>

    @GET("sdapi/v1/sd-models")
    suspend fun models(): List<SDModelsBody>

    @POST("sdapi/v1/sd-models")
    suspend fun model(@Body name: String)

    @GET("sdapi/v1/options")
    suspend fun options(): OptionsBody

    @POST("sdapi/v1/options")
    suspend fun options(@Body body: Map<String, @JvmSuppressWildcards Any>)

    @POST("sdapi/v1/txt2img")
    suspend fun text2Image(@Body text2ImageBody: Text2ImageBody): Text2ImageResponse

    @POST("sdapi/v1/img2img")
    suspend fun image2Image(@Body image2ImageBody: Image2ImageBody): Image2ImageResponse
}

data class OptionsBody(
    val sd_model_checkpoint: String
)

data class SDModelsBody(
    val title: String
)

data class SamplersBody(
    val name: String
)

data class Text2ImageBody(
    val prompt: String,
    val steps: Int = 20,
    val cfg_scale: Float = 7f,
    val sampler_name: String = "Euler",
    val width: Int = 768,
    val height: Int = 768
)

data class Text2ImageResponse(
    val images: List<String>
)

data class Image2ImageBody(
    val prompt: String,
    val steps: Int,
    val cfg_scale: Float,
    val sampler_name: String,
    val init_images: List<String>,
    val denoising_strength: Float,
    val include_init_images: Boolean = true,
    val width: Int = 768,
    val height: Int = 768
)

data class Image2ImageResponse(
    val images: List<String>
)

data class ProgressBody(
    val progress: Float,
    val eta_relative: Float,
    val state: ProgressState,
) {
    data class ProgressState(
        val skipped: Boolean,
        val interrupted: Boolean,
        val job: String,
        val job_count: Int,
        val job_no: Int,
        val sampling_step: Int,
        val sampling_steps: Int
    )
}