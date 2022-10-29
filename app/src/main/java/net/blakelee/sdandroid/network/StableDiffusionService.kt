package net.blakelee.sdandroid.network

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StableDiffusionService {

    @GET("login_check")
    suspend fun isLoggedIn(): Boolean

    @GET("queue/status")
    suspend fun queueStatus(): JsonObject

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