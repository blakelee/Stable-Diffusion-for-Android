package net.blakelee.sdandroid.img2img

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun Image2ImageScreen(viewModel: Image2ImageViewModel) {

    LaunchedEffect(Unit) {
        viewModel.init()
    }
}