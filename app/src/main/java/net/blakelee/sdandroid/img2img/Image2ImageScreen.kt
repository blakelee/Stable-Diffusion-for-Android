package net.blakelee.sdandroid.img2img

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import net.blakelee.sdandroid.AppNavGraph

@AppNavGraph
@Destination
@Composable
fun Image2ImageScreen(
    navController: NavController,
    viewModel: Image2ImageViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.init()
    }
}