package net.blakelee.sdandroid.img2img

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import net.blakelee.sdandroid.Submit
import net.blakelee.sdandroid.combine
import net.blakelee.sdandroid.img2img.Image2ImageWorkflow.State
import javax.inject.Inject

class Image2ImageWorkflow @Inject constructor(
    private val cache: Image2ImageCache
) : StatefulWorkflow<Unit, State, Submit, ComposeScreen>() {

    private val stateFlow: Flow<State> = combine(
        cache.prompt,
        cache.prompts,
        cache.denoisingStrength,
        cache.selectedImage,
        cache.images,
        flowOf<Action?>(null),
        flowOf(false),
        ::State
    )

    private val updateState: MutableStateFlow<suspend () -> Unit> = MutableStateFlow {}

    data class Action(
        private val name: String,
        val runnable: suspend () -> Unit
    ) {
        val key get() = "$name+${hashCode()}"
    }

    data class State(
        val prompt: String = "",
        val prompts: Set<String> = setOf(),
        val denoisingStrength: Float = 0.75f,
        val selectedImage: Bitmap? = null,
        val images: List<Bitmap> = emptyList(),
        val action: Action? = null,
        val isCropping: Boolean = false
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {
        if (renderState.isCropping && renderState.selectedImage != null)
            return CropScreen(
                bitmap = renderState.selectedImage,
                onBackPressed = context.eventHandler {
                    state = state.copy(isCropping = false)
                },
                onImageCropped = { bitmap ->
                    context.eventHandler {
                        state = state.copy(
                            isCropping = false,
                            action = setBitmapAction(bitmap)
                        )
                    }()
                }
            )


        context.runningWorker(stateFlow.asWorker(), handler = ::updateState)

        context.runningWorker(
            updateState.map { it() }.asWorker(),
            handler = { WorkflowAction.noAction() })

        renderState.action?.let { action ->
            context.runningSideEffect(action.key) {
                action.runnable()
                context.eventHandler { state = state.copy(action = null) }
            }
        }

        return Image2ImageScreen(
            prompt = renderState.prompt,
            prompts = renderState.prompts,
            onPromptChanged = { context.actionSink.send(setPrompt(it)) },
            onPromptDeleted = { context.actionSink.send(deletePrompt(it)) },
            onSubmit = context.eventHandler { setOutput(Submit) },
            denoisingStrength = renderState.denoisingStrength.toString(),
            onDenoisingStrengthChanged = { context.actionSink.send(setDenoisingStrength(it)) },
            selectedImage = renderState.selectedImage,
            images = renderState.images,
            onImageSelected = { bitmap ->
                context.actionSink.send(action {
                    state = state.copy(
                        isCropping = true,
                        selectedImage = bitmap,
                        action = setBitmapAction(bitmap)
                    )
                })
            }
        )
    }

    private fun setBitmapAction(bitmap: Bitmap?): Action = Action("selectedImage") {
        cache.setSelectedImage(bitmap)
    }

    private fun setPrompt(prompt: String) = setAction("prompt") {
        cache.setPrompt(prompt)
    }

    private fun deletePrompt(prompt: String) = setAction("deletePrompt") {
        cache.deletePrompt(prompt)
    }

    private fun setDenoisingStrength(denoisingStrength: String) = setAction("denoisingStrength") {
        val denoisingStrength = denoisingStrength.toFloatOrNull() ?: 0f
        cache.setDenoisingStrength(denoisingStrength)
    }

    private fun updateState(state: State) = action {
        this.state = this.state.copy(
            prompt = state.prompt,
            prompts = state.prompts,
            denoisingStrength = state.denoisingStrength,
            selectedImage = state.selectedImage,
            images = state.images,
            action = this.state.action,
            isCropping = this.state.isCropping
        )
    }

    private fun setAction(name: String, runnable: suspend () -> Unit) = action {
        state = state.copy(action = Action(name, runnable))
    }

    override fun snapshotState(state: State): Snapshot? = null

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State(
        prompt = runBlocking { cache.prompt.first() }
    )
}
