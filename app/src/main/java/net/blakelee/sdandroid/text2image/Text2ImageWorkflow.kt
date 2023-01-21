package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.TextController
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import net.blakelee.sdandroid.settings.SettingsCache
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow.State
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Text2ImageWorkflow @Inject constructor(
    private val cache: SettingsCache
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    data class State(
        val prompt: TextController = TextController(),
        val prompts: Set<String> = setOf(),
        val images: List<Bitmap> = emptyList(),
        val onPromptDelete: String? = null
    )

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State(
        prompt = runBlocking { TextController(cache.prompt.first()) }
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

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

        return Text2ImageScreen(
            prompt = renderState.prompt,
            prompts = renderState.prompts,
            onPromptDelete = context.onPromptDelete(),
            onSubmit = context.eventHandler { setOutput(Unit) },
            images = renderState.images
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
}
