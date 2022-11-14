package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.compose.ComposeScreen
import dagger.multibindings.ClassKey
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow.Props
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow.State
import javax.inject.Inject

@ClassKey(Text2ImageWorkflow::class)
class Text2ImageWorkflow @Inject constructor() :
    StatefulWorkflow<Props, State, Unit, ComposeScreen>() {

    data class Props(
        val images: List<Bitmap>,
        val onSubmit: () -> Unit
    )

    data class State(
        val prompt: String = "",
        val prompts: Set<String> = setOf(),
        val cfgScale: Float = 7f,
        val steps: Int = 25,
        val images: List<Bitmap> = emptyList()
    )

    override fun initialState(props: Props, snapshot: Snapshot?): State =
        State(images = props.images)

    override fun render(
        renderProps: Props,
        renderState: State,
        context: RenderContext
    ): ComposeScreen = Text2ImageScreen(
        prompt = renderState.prompt,
        onPromptChanged = { context.actionSink.send(prompt(it)) },
        prompts = renderState.prompts,
        onPromptDeleted = {},
        onSubmit = renderProps.onSubmit,
        cfgScale = renderState.cfgScale.toString(),
        onCfgScaleChanged = { context.actionSink.send(cfgScale(it)) },
        steps = renderState.steps.toString(),
        onStepsChanged = { context.actionSink.send(steps(it)) },
        images = renderProps.images
    )

    val prompt = fun(prompt: String) = action {
        state = state.copy(prompt = prompt)
    }

    val cfgScale = fun(cfgScale: String) = action {
        val cfgScaleFloat = cfgScale.toFloatOrNull() ?: 0f
        state = state.copy(cfgScale = cfgScaleFloat)
    }

    val steps = fun(steps: String) = action {
        val stepsInt = steps.filter { it.isDigit() }.toIntOrNull() ?: 0
        state = state.copy(steps = stepsInt)
    }

    override fun snapshotState(state: State): Snapshot? = null
}
