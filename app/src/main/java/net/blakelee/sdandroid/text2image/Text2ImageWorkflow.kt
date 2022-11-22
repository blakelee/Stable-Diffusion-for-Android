package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.blakelee.sdandroid.combine
import net.blakelee.sdandroid.settings.SettingsCache
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow.State
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Text2ImageWorkflow @Inject constructor(
    private val cache: Text2ImageCache,
    private val settingsCache: SettingsCache
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

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

    sealed class Action {
        data class UpdatePrompt(val prompt: String) : Action()
        data class UpdateCfgScale(val cfgScale: Float) : Action()
        data class UpdateSteps(val steps: Int) : Action()
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

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(stateFlow.asWorker(), handler = ::updateState)

        context.runningWorker(updateState.map { it() }.asWorker(), handler = { noAction() })

        when (renderState.action) {
            is Action.UpdateCfgScale -> context.runningSideEffect("cfgScale") {
                cache.setCfgScale(renderState.action.cfgScale)
                context.eventHandler { state = state.copy(action = null) }
            }
            is Action.UpdatePrompt -> context.runningSideEffect("prompt") {
                cache.setPrompt(renderState.action.prompt)
                context.eventHandler { state = state.copy(action = null) }
            }
            is Action.UpdateSteps -> context.runningSideEffect("steps") {
                cache.setSteps(renderState.action.steps)
                context.eventHandler { state = state.copy(action = null) }
            }
            else -> {}
        }

        return Text2ImageScreen(
            prompt = renderState.prompt,
            onPromptChanged = { context.actionSink.send(setPrompt(it)) },
            prompts = renderState.prompts,
            onPromptDeleted = {},
            onSubmit = context.eventHandler { setOutput(Unit) },
            cfgScale = renderState.cfgScale.toString(),
            onCfgScaleChanged = { context.actionSink.send(setCfgScale(it)) },
            steps = renderState.steps.toString(),
            onStepsChanged = { context.actionSink.send(setSteps(it)) },
            images = renderState.images
        )
    }

    private fun setPrompt(prompt: String) = action {
        state = state.copy(action = Action.UpdatePrompt(prompt))
    }

    private fun setCfgScale(cfgScale: String) = action {
        val cfgScaleFloat = cfgScale.toFloatOrNull() ?: 0f
        state = state.copy(action = Action.UpdateCfgScale(cfgScaleFloat))
    }

    private fun setSteps(steps: String) = action {
        val stepsInt = steps.filter { it.isDigit() }.toIntOrNull() ?: 0
        state = state.copy(action = Action.UpdateSteps(stepsInt))
    }

    private fun updateState(state: State) = action {
        this.state = state
    }

    override fun snapshotState(state: State): Snapshot? = null
}
