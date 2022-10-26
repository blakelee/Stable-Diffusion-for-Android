package net.blakelee.sdandroid.landing

import net.blakelee.sdandroid.network.StableDiffusionService
import javax.inject.Inject

class LoginRepository @Inject constructor(private val service: StableDiffusionService) {

    suspend fun isLoggedIn() = service.isLoggedIn()
}