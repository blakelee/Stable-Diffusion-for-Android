package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow.State
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Text2ImageWorkflow @Inject constructor(
    private val cache: Text2ImageCache
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    private val stateFlow: Flow<State> = combine(
        cache.prompt,
        cache.prompts,
        cache.cfgScale,
        cache.steps,
        cache.images,
        ::State
    )

    private val updateState: MutableStateFlow<suspend () -> Unit> = MutableStateFlow {}

    data class State(
        val prompt: String = "",
        val prompts: Set<String> = setOf(),
        val cfgScale: Float = 7f,
        val steps: Int = 25,
        val images: List<Bitmap> = emptyList()
    )

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(stateFlow.asWorker(), handler = ::updateState)

        context.runningWorker(updateState.map { it() }.asWorker(), handler = { noAction() })

        return Text2ImageScreen(
            prompt = renderState.prompt,
            onPromptChanged = { setPrompt(it) },
            prompts = renderState.prompts,
            onPromptDeleted = {},
            onSubmit = context.eventHandler { setOutput(Unit) },
            cfgScale = renderState.cfgScale.toString(),
            onCfgScaleChanged = { setCfgScale(it) },
            steps = renderState.steps.toString(),
            onStepsChanged = { setSteps(it) },
            images = renderState.images
        )
    }

    private fun setPrompt(prompt: String) {
        updateState.tryEmit {
            cache.setPrompt(prompt)
        }

    }

    private fun setCfgScale(cfgScale: String) {
        val cfgScaleFloat = cfgScale.toFloatOrNull() ?: 0f
        updateState.tryEmit {
            cache.setCfgScale(cfgScaleFloat)
        }
    }

    private fun setSteps(steps: String) {
        val stepsInt = steps.filter { it.isDigit() }.toIntOrNull() ?: 0
        updateState.tryEmit {
            cache.setSteps(stepsInt)
        }
    }

    private fun updateState(state: State) = action {
        this.state = state
    }

    override fun snapshotState(state: State): Snapshot? = null
}
