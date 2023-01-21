package net.blakelee.sdandroid.img2img

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import net.blakelee.sdandroid.Submit
import net.blakelee.sdandroid.img2img.Image2ImageWorkflow.State
import javax.inject.Inject

class Image2ImageWorkflow @Inject constructor(
    private val cache: Image2ImageCache
) : StatefulWorkflow<Unit, State, Submit, ComposeScreen>() {

    private val updateCache = MutableSharedFlow<suspend () -> Unit>()

    data class State(
        val prompt: TextController,
        val prompts: Set<String> = setOf(),
        val denoisingStrength: Float = 0.75f,
        val selectedImage: Bitmap? = null,
        val images: List<Bitmap> = emptyList(),
        val isCropping: Boolean = false,
        val onPromptDelete: String? = null
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {
        context.runningSideEffect(renderState.selectedImage?.hashCode().toString()) {
            cache.setSelectedImage(renderState.selectedImage)
        }

        context.runningWorker(
            renderState.prompt.onTextChanged
                .onEach(cache::setPrompt)
                .asWorker(), "prompt"
        ) { WorkflowAction.noAction() }

        context.runningWorker(cache.images.asWorker(), handler = setImages)
        context.runningWorker(cache.prompts.asWorker(), handler = setPrompts)

        renderState.onPromptDelete?.let {
            context.runningSideEffect(it) { cache.deletePrompt(it) }
        }

        context.runningSideEffect(renderState.denoisingStrength.toString()) {
            cache.setDenoisingStrength(renderState.denoisingStrength)
        }

        if (renderState.isCropping && renderState.selectedImage != null)
            return CropScreen(
                bitmap = renderState.selectedImage,
                onBackPressed = context.eventHandler { state = state.copy(isCropping = false) },
                onImageCropped = context.eventHandler { bitmap ->
                    state = state.copy(isCropping = false, selectedImage = bitmap)
                }
            )

        return Image2ImageScreen(
            prompt = renderState.prompt,
            prompts = renderState.prompts,
            onPromptDelete = context.onPromptDelete(),
            onSubmit = context.eventHandler { setOutput(Submit) },
            denoisingStrength = renderState.denoisingStrength.toString(),
            onDenoisingStrengthChange = context.eventHandler { it ->
                state = state.copy(denoisingStrength = it.toFloatOrNull() ?: 0f)
            },
            selectedImage = renderState.selectedImage,
            images = renderState.images,
            onImageSelected = context.eventHandler { bitmap ->
                state = state.copy(
                    isCropping = true,
                    selectedImage = bitmap
                )
            }
        )
    }

    private val setPrompts = { prompts: Set<String> ->
        action { state = state.copy(prompts = prompts) }
    }

    private val setImages = { images: List<Bitmap> ->
        action { state = state.copy(images = images) }
    }

    private fun RenderContext.onPromptDelete(): (String) -> Unit {
        return eventHandler { prompt -> state = state.copy(onPromptDelete = prompt) }
    }

    override fun snapshotState(state: State): Snapshot? = null

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State(
        prompt = runBlocking { TextController(cache.prompt.first()) },
        prompts = runBlocking { cache.prompts.first() },
        denoisingStrength = runBlocking { cache.denoisingStrength.first() },
        selectedImage = cache.selectedImage.value,
        images = cache.images.value,
    )
}
