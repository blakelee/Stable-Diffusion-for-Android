package net.blakelee.sdandroid.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StableDiffusionService {

    @GET("login_check")
    suspend fun isLoggedIn(): Boolean

    @POST("sdapi/v1/txt2img")
    suspend fun text2Image(@Body text2ImageBody: Text2ImageBody): Text2ImageResponse
}

data class Text2ImageBody(
    val prompt: String,
    val enable_hr: Boolean = false,
    val denoising_strength: Float = 0f,
    val firstphase_width: Int = 0,
    val firstphase_height: Int = 0,
    val styles: List<String> = emptyList(),
    val seed: Int = -1,
    val subseed: Int = -1,
    val subseed_strength: Int = 0,
    val seed_resize_from_h: Int = -1,
    val seed_resize_from_w: Int = -1,
    val batch_size: Int = 1,
    val n_iter: Int = 1,
    val steps: Int = 20,
    val cfg_scale: Float = 7f,
    val width: Int = 512,
    val height: Int = 512,
    val restore_faces: Boolean = false,
    val tiling: Boolean = false,
    val negative_prompt: String = "",
    val eta: Int = 0,
    val s_churn: Int = 0,
    val s_tmax: Int = 0,
    val s_tmin: Int = 0,
    val s_noise: Int = 0,
    val sampler_index: String = "Euler"
)

data class Text2ImageResponse(
    val images: List<String>
)