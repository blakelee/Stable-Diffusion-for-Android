package net.blakelee.sdandroid.img2img

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.asMutableState
import net.blakelee.sdandroid.compose.prompt
import net.blakelee.sdandroid.text2image.PagerButtons
import net.blakelee.sdandroid.text2image.RenderImage
import net.blakelee.sdandroid.ui.theme.padding


data class Image2ImageScreen(
    val prompt: TextController,
    val prompts: Set<String>,
    val onPromptDelete: (String) -> Unit,
    val denoisingStrength: String,
    val onDenoisingStrengthChange: (String) -> Unit,
    val selectedImage: Bitmap? = null,
    val onImageSelected: (Bitmap) -> Unit,
    val images: List<Bitmap> = emptyList(),
    val onSubmit: () -> Unit
) : ComposeScreen {

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        var prompt by prompt.asMutableState()
        Column(modifier = Modifier.padding(padding)) {

            prompt(
                prompts = prompts,
                onPromptDeleted = onPromptDelete,
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                onSubmit = onSubmit
            )

            var sliderPosition by remember { mutableStateOf(75) }
            val sliderPositionString by remember {
                derivedStateOf { String.format("%.2f", sliderPosition / 100f) }
            }

            Text(text = "Denoising Strength: $sliderPositionString")
            Slider(
                value = sliderPosition / 100f,
                onValueChange = { sliderPosition = (it * 100).toInt() },
                steps = 100,
                onValueChangeFinished = { onDenoisingStrengthChange(sliderPositionString) }
            )

            val contentResolver = LocalContext.current.contentResolver

            val launcher = rememberLauncherForActivityResult(
                contract = PickVisualMedia(),
                onResult = {
                    it?.let {
                        val bitmap = it.getBitmap(contentResolver)
                        onImageSelected(bitmap)
                    }
                }
            )

            Row {
                Button(onClick = { launcher.launch(PickVisualMediaRequest(ImageOnly)) }) {
                    Text("Select Image")
                }

                selectedImage?.let {
                    RenderImage(bitmap = it, modifier = Modifier.size(48.dp))
                }
            }

            val pagerState = rememberPagerState()

            Box {
                HorizontalPager(count = images.size, state = pagerState) { pager ->
                    RenderImage(bitmap = images[pager], modifier = Modifier.fillMaxWidth())
                }

                // Start at the first image when we get a new set of images
                LaunchedEffect(images.hashCode()) {
                    pagerState.scrollToPage(0)
                }

                if (images.size > 1) {
                    PagerButtons(
                        numImages = images.size,
                        pagerState = pagerState,
                        Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

private fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, this)
        ImageDecoder.decodeBitmap(source)
    } else {
        MediaStore.Images.Media.getBitmap(contentResolver, this)
    }
}