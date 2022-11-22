package net.blakelee.sdandroid

import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import dagger.multibindings.ClassKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.blakelee.sdandroid.PrimaryWorkflow.State
import net.blakelee.sdandroid.img2img.Image2ImageWorkflow
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.main.BottomBarItem
import net.blakelee.sdandroid.main.MainScreen
import net.blakelee.sdandroid.network.StableDiffusionRepository
import net.blakelee.sdandroid.settings.SettingsWorkflow
import net.blakelee.sdandroid.text2image.Text2ImageCache
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow
import javax.inject.Inject

@ClassKey(PrimaryWorkflow::class)
class PrimaryWorkflow @Inject constructor(
    private val t2iWorkflow: Text2ImageWorkflow,
    private val loginRepository: LoginRepository,
    private val text2Image: Text2ImageCache,
    private val sdRepo: StableDiffusionRepository,
    private val settingsWorkflow: SettingsWorkflow
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {


    sealed class State {
        abstract val selectedItem: BottomBarItem

        data class Processing(
            override val selectedItem: BottomBarItem,
            val progress: Float
        ) : State()

        data class Default(override val selectedItem: BottomBarItem) : State()
    }

    enum class SubmitType {
        Text2Image,
        Image2Image
    }

    data class ProcessState(
        val processing: Boolean = false,
        val progress: Float = 0f
    )

    private val submitTypeFlow =
        MutableSharedFlow<SubmitType?>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val processFlow: Flow<ProcessState> = submitTypeFlow.flatMapMerge { type ->
        withContext(Dispatchers.IO) {
            when (type) {
                SubmitType.Text2Image -> processText2Image()
                else -> interrupt()
            }
        }
    }

    private suspend fun processText2Image(): Flow<ProcessState> = text2Image.submit()
        .combine(progressFlow(), ::ProcessState)

    private suspend fun interrupt(): Flow<ProcessState> = flow {
        runCatching { sdRepo.interrupt() }
        emit(ProcessState())
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State =
        State.Default(BottomBarItem.Text2Image)

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        if (renderState is State.Processing) {
            context.runningWorker(processFlow.asWorker()) {
                action {
                    state = when (it.processing) {
                        true -> State.Processing(selectedItem = state.selectedItem, it.progress)
                        false -> State.Default(selectedItem = state.selectedItem)
                    }
                }
            }

            context.runningSideEffect("submit") {
                val submitType = when (renderState.selectedItem) {
                    BottomBarItem.Text2Image -> SubmitType.Text2Image
                    BottomBarItem.Image2Image -> SubmitType.Image2Image
                    else -> null
                }
                submitTypeFlow.emit(submitType)
            }
        }

        if (renderState is State.Default) {
            context.runningSideEffect("cancel") { submitTypeFlow.emit(null) }
        }

        return MainScreen(
            onBack = loginRepository::logout,
            onCancel = context.eventHandler { state = State.Default(state.selectedItem) },
            onSubmit = context.eventHandler { state = State.Processing(state.selectedItem, 0f) },
            progress = (renderState as? State.Processing)?.progress ?: 0f,
            processing = renderState is State.Processing,
            selectedItem = renderState.selectedItem,
            onItemSelected = { item ->
                context.eventHandler {
                    state = when (val state = state) {
                        is State.Processing -> state.copy(selectedItem = item)
                        is State.Default -> state.copy(selectedItem = item)
                    }
                }()
            },
            screen = nextState(context, renderState)
        )
    }

    private fun nextState(
        context: BaseRenderContext<Unit, State, Unit>,
        state: State
    ): ComposeScreen {

        return when (state.selectedItem) {
            BottomBarItem.Text2Image -> context.renderChild(t2iWorkflow, Unit) {
                submitTypeFlow.tryEmit(SubmitType.Text2Image)
                noAction()

            }
            BottomBarItem.Image2Image ->
                context.renderChild(Image2ImageWorkflow, Unit) { noAction() }

            BottomBarItem.Settings ->
                context.renderChild(settingsWorkflow, Unit) { noAction() }
        }
    }

    private fun progressFlow(): Flow<Float> = flow {
        runCatching {
            emit(0f)

            var progress = 0f
            do {
                delay(250)
                val newProgress = runCatching { sdRepo.progress().progress }.getOrNull() ?: 0f
                emit(newProgress)
                progress = maxOf(newProgress, progress)

            } while (newProgress >= progress)

            emit(1f)
        }
    }

    override fun snapshotState(state: State): Snapshot? = null
}