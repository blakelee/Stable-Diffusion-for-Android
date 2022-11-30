package net.blakelee.sdandroid.img2img

import android.graphics.Bitmap
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.Submit
import net.blakelee.sdandroid.img2img.Image2ImageWorkflow.State
import javax.inject.Inject

class Image2ImageWorkflow @Inject constructor(
    private val cache: Image2ImageCache
) : StatefulWorkflow<Unit, State, Submit, ComposeScreen>() {

    data class State(
        val image: Bitmap? = null
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        return Image2ImageScreen(
            prompt = "",
            prompts = setOf(),
            onPromptChanged = {},
            onPromptDeleted = {},
            cfgScale = "7.5",
            onCfgScaleChanged = {},
            steps = "25",
            onStepsChanged = {},
            onSubmit = {},
            denoisingStrength = "0.75",
            onDenoisingStrengthChanged = {},
            selectedImage = renderState.image,
            onImageSelected = {
                context.eventHandler { state = state.copy(image = it) }()
            }
        )
    }

    override fun snapshotState(state: State): Snapshot? = null

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()
}
