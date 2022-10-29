package net.blakelee.sdandroid.img2img

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import net.blakelee.sdandroid.AppNavGraph

@AppNavGraph
@Destination
@Composable
fun Image2ImageScreen(navController: NavController) {
    Text("Hello world")
}