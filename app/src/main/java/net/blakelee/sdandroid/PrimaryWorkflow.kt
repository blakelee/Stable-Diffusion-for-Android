package net.blakelee.sdandroid

import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import dagger.multibindings.ClassKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.PrimaryWorkflow.State
import net.blakelee.sdandroid.img2img.Image2ImageWorkflow
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.main.BottomBarItem
import net.blakelee.sdandroid.main.MainScreen
import net.blakelee.sdandroid.network.StableDiffusionRepository
import net.blakelee.sdandroid.settings.SettingsCache
import net.blakelee.sdandroid.settings.SettingsWorkflow
import net.blakelee.sdandroid.text2image.Text2ImageCache
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow
import javax.inject.Inject

object Submit

@ClassKey(PrimaryWorkflow::class)
class PrimaryWorkflow @Inject constructor(
    private val t2iWorkflow: Text2ImageWorkflow,
    private val loginRepository: LoginRepository,
    private val text2Image: Text2ImageCache,
    private val sdRepo: StableDiffusionRepository,
    private val settingsWorkflow: SettingsWorkflow,
    private val settingsCache: SettingsCache
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    data class State(
        val tab: BottomBarItem = BottomBarItem.Text2Image,
        val submit: BottomBarItem? = null,
        val progress: Float? = null
    )

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        when (renderState.submit) {
            BottomBarItem.Text2Image -> context.runningWorker(text2Image(), handler = progress)
            BottomBarItem.Image2Image -> context.runningWorker(image2Image(), handler = progress)
            else -> {}
        }

        return MainScreen(
            onBack = loginRepository::logout,
            onCancel = context.eventHandler { state = state.copy(submit = null) },
            onSubmit = context.eventHandler { state = state.copy(submit = state.tab) },
            progress = renderState.progress ?: 0f,
            processing = renderState.submit != null,
            selectedItem = renderState.tab,
            onItemSelected = { item ->
                context.eventHandler {
                    state = state.copy(tab = item)
                }()
            },
            screen = nextState(context, renderState)
        )
    }

    private fun nextState(
        context: BaseRenderContext<Unit, State, Unit>,
        state: State
    ): ComposeScreen {
        val action = action { this.state = this.state.copy(submit = state.tab) }

        return when (state.tab) {
            BottomBarItem.Text2Image -> context.renderChild(t2iWorkflow, Unit, handler = { action })
            BottomBarItem.Image2Image ->
                context.renderChild(Image2ImageWorkflow, Unit, handler = { action })
            BottomBarItem.Settings -> context.renderChild(settingsWorkflow, Unit) { noAction() }
        }
    }

    private val progress = fun(progress: Float?) = action {
        val submit = if (progress != null) state.submit else null
        state = state.copy(submit = submit, progress = progress)
    }

    private fun text2Image(): Worker<Float?> = flow {
        val sampler = settingsCache.sampler.first()
        emitAll(text2Image.submit(sampler)
            .combine(progressFlow()) { processing, progress ->
                if (processing) progress else null
            }
        )
    }.asWorker()

    private fun image2Image(): Worker<Float?> = flowOf<Float?>(null).asWorker()

    private fun progressFlow(): Flow<Float> = flow {
        runCatching {
            emit(0f)

            var progress = 0f
            do {
                delay(200)
                val newProgress = runCatching { sdRepo.progress().progress }.getOrNull() ?: 0f
                emit(newProgress)
                progress = maxOf(newProgress, progress)

            } while (newProgress >= progress)

            emit(1f)
        }
    }

    override fun snapshotState(state: State): Snapshot? = null
}