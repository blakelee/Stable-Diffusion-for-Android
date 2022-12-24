package net.blakelee.sdandroid.img2img

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen

data class CropScreen(
    val bitmap: Bitmap,
    val onBackPressed: () -> Unit,
    val onImageCropped: (Bitmap) -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {

        BackHandler(true, onBackPressed)

        var crop by remember { mutableStateOf(false) }
        val imageBitmap = remember { bitmap.asImageBitmap() }

        ImageCropper(
            imageBitmap = imageBitmap,
            contentDescription = null,
            cropProperties = CropDefaults.properties(
                aspectRatio = AspectRatio(1f),
                cropOutlineProperty = CropOutlineProperty(
                    OutlineType.Rect,
                    RectCropShape(id = 1, title = "square")
                ),
                overlayRatio = 1f
            ),
            crop = crop,
            onCropStart = {},
            onCropSuccess = {
                onImageCropped(it.asAndroidBitmap())
            }
        )

        Button(onClick = { crop = true }) {
            Text("Crop")
        }
    }
}