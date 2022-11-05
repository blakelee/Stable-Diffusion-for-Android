package net.blakelee.sdandroid.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StableDiffusionService {

    @GET("login_check")
    suspend fun isLoggedIn(): Boolean

    @GET("sdapi/v1/progress")
    suspend fun progress(): ProgressBody

    @POST("sdapi/v1/txt2img")
    suspend fun text2Image(@Body text2ImageBody: Text2ImageBody): Text2ImageResponse
}

data class Text2ImageBody(
    val prompt: String,
    val steps: Int = 20,
    val cfg_scale: Float = 7f,
)

data class Text2ImageResponse(
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