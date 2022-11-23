package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import net.blakelee.sdandroid.Submit
import net.blakelee.sdandroid.combine
import net.blakelee.sdandroid.settings.SettingsCache
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow.State
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Text2ImageWorkflow @Inject constructor(
    private val cache: Text2ImageCache,
    settingsCache: SettingsCache
) : StatefulWorkflow<Unit, State, Submit, ComposeScreen>() {

    private val stateFlow: Flow<State> = combine(
        cache.prompt,
        cache.prompts,
        cache.cfgScale,
        cache.steps,
        cache.images,
        settingsCache.sampler,
        flowOf<Action?>(null),
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
        val cfgScale: Float = 7f,
        val steps: Int = 25,
        val images: List<Bitmap> = emptyList(),
        val sampler: String = "Euler a",
        val action: Action? = null
    )

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State(
        prompt = runBlocking { cache.prompt.first() }
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(stateFlow.asWorker(), handler = ::updateState)

        context.runningWorker(updateState.map { it() }.asWorker(), handler = { noAction() })

        renderState.action?.let { action ->
            context.runningSideEffect(action.key) {
                action.runnable()
                context.eventHandler { state = state.copy(action = null) }
            }
        }

        return Text2ImageScreen(
            prompt = renderState.prompt,
            onPromptChanged = { context.actionSink.send(setPrompt(it)) },
            prompts = renderState.prompts,
            onPromptDeleted = { context.actionSink.send(deletePrompt(it)) },
            onSubmit = context.eventHandler { setOutput(Submit) },
            cfgScale = renderState.cfgScale.toString(),
            onCfgScaleChanged = { context.actionSink.send(setCfgScale(it)) },
            steps = renderState.steps.toString(),
            onStepsChanged = { context.actionSink.send(setSteps(it)) },
            images = renderState.images
        )
    }

    private fun setPrompt(prompt: String) = setAction("prompt") {
        cache.setPrompt(prompt)
    }

    private fun deletePrompt(prompt: String) = setAction("deletePrompt") {
        cache.deletePrompt(prompt)
    }

    private fun setCfgScale(cfgScale: String) = setAction("cfgScale") {
        val cfgScaleFloat = cfgScale.toFloatOrNull() ?: 0f
        cache.setCfgScale(cfgScaleFloat)
    }

    private fun setSteps(steps: String) = setAction("steps") {
        val stepsInt = steps.filter { it.isDigit() }.toIntOrNull() ?: 0
        cache.setSteps(stepsInt)
    }

    private fun updateState(state: State) = action {
        this.state = state
    }

    private fun setAction(name: String, runnable: suspend () -> Unit) = action {
        state = state.copy(action = Action(name, runnable))
    }

    override fun snapshotState(state: State): Snapshot? = null
}
