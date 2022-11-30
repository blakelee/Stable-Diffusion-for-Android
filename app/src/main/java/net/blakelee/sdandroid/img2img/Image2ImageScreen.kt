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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.compose.config
import net.blakelee.sdandroid.compose.prompt
import net.blakelee.sdandroid.compose.steps
import net.blakelee.sdandroid.text2image.renderImage
import net.blakelee.sdandroid.ui.theme.padding


data class Image2ImageScreen(
    val prompt: String,
    val onPromptChanged: (String) -> Unit,
    val prompts: Set<String>,
    val onPromptDeleted: (String) -> Unit,
    val cfgScale: String,
    val onCfgScaleChanged: (String) -> Unit,
    val steps: String,
    val onStepsChanged: (String) -> Unit,
    val denoisingStrength: String,
    val onDenoisingStrengthChanged: (String) -> Unit,
    val selectedImage: Bitmap? = null,
    val onImageSelected: (Bitmap) -> Unit,
    val onSubmit: () -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Column(modifier = Modifier.padding(padding)) {

            prompt(
                prompts = prompts,
                onPromptDeleted = onPromptDeleted,
                value = prompt,
                onValueChange = onPromptChanged,
                modifier = Modifier.fillMaxWidth(),
                onSubmit = onSubmit
            )

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                config(
                    value = cfgScale,
                    modifier = Modifier.weight(0.5f),
                    onValueChange = { onCfgScaleChanged(it) }
                )

                Spacer(Modifier.width(8.dp))

                steps(
                    value = steps,
                    modifier = Modifier.weight(0.5f),
                    onValueChange = onStepsChanged,
                    onSubmit = onSubmit
                )
            }

            var sliderPosition by remember { mutableStateOf(75) }
            val sliderPositionString by remember {
                derivedStateOf { String.format("%.2f", sliderPosition / 100f) }
            }

            Text(text = "Denoising Strength: $sliderPositionString")
            Slider(
                value = sliderPosition / 100f,
                onValueChange = { sliderPosition = (it * 100).toInt() },
                steps = 100,
                onValueChangeFinished = { onDenoisingStrengthChanged(sliderPositionString) }
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
                    renderImage(bitmap = it, modifier = Modifier.size(48.dp))
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