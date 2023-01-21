package net.blakelee.sdandroid.img2img

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen


data class ImagePicker(
    val onBackPressed: () -> Unit,
    val onImagePicked: (Bitmap) -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        BackHandler { onBackPressed() }

        val contentResolver = LocalContext.current.contentResolver

        val launcher = rememberLauncherForActivityResult(
            contract = PickVisualMedia(),
            onResult = {
                it?.let {
                    val bitmap = it.getBitmap(contentResolver)
                    onImagePicked(bitmap)
                } ?: run {
                    onBackPressed()
                }
            }
        )

        LaunchedEffect(Unit) {
            launcher.launch(PickVisualMediaRequest(ImageOnly))
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