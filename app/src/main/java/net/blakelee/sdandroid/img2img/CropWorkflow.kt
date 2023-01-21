package net.blakelee.sdandroid.img2img

import android.graphics.Bitmap
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.img2img.CropWorkflow.State
import net.blakelee.sdandroid.img2img.CropWorkflow.State.CropImage

object CropWorkflow : StatefulWorkflow<Bitmap?, State, Bitmap?, ComposeScreen>() {
    sealed class State {
        object PickImage : State()
        data class CropImage(val bitmap: Bitmap) : State()
    }

    override fun initialState(props: Bitmap?, snapshot: Snapshot?) =
        props?.let(::CropImage) ?: State.PickImage

    override fun snapshotState(state: State): Snapshot? = null

    override fun render(
        renderProps: Bitmap?,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {
        if (renderState is State.PickImage) {
            return ImagePicker(
                onBackPressed = context.eventHandler { setOutput(null) },
                onImagePicked = context.eventHandler { bitmap ->
                    state = CropImage(bitmap)
                }
            )
        }

        return CropScreen(
            bitmap = (renderState as CropImage).bitmap,
            onBackPressed = context.eventHandler { setOutput(null) },
            onImageCropped = context.eventHandler { bitmap -> setOutput(bitmap) }
        )
    }
}
