package net.blakelee.sdandroid

import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import dagger.multibindings.ClassKey
import kotlinx.coroutines.flow.*
import net.blakelee.sdandroid.PrimaryWorkflow.State
import net.blakelee.sdandroid.img2img.CropWorkflow
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.main.MainScreen
import net.blakelee.sdandroid.main.SheetItem
import net.blakelee.sdandroid.settings.SettingsCache
import net.blakelee.sdandroid.settings.SettingsWorkflow
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow
import javax.inject.Inject

@ClassKey(PrimaryWorkflow::class)
class PrimaryWorkflow @Inject constructor(
    private val t2iWorkflow: Text2ImageWorkflow,
    private val settingsWorkflow: SettingsWorkflow,
    private val loginRepository: LoginRepository,
    private val settingsCache: SettingsCache,
    private val repository: StableDiffusionRepository
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    data class State(
        val tab: SheetItem = SheetItem.Text2Image,
        val submit: Boolean = false,
        val progress: Float? = null,
        val isCropping: Boolean = false,
        val error: String? = null
    )

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {
        if (renderState.submit) {
            context.runningWorker(repository.submit().asWorker()) {
                val isSubmit = it != -1f
                when (it) {
                    is Float -> action {
                        state = state.copy(progress = it, submit = isSubmit, error = null)
                    }
                    is String -> action { state = state.copy(error = it) }
                    else -> noAction()
                }
            }
        }

        var cropChild: ComposeScreen? = null
        if (renderState.isCropping) {
            cropChild = context.renderChild(
                CropWorkflow,
                settingsCache.selectedImage.value
            ) {
                val tab = if (it != null) SheetItem.Image2Image else SheetItem.Text2Image
                settingsCache.selectedImage.tryEmit(it)
                action { state = state.copy(isCropping = false, tab = tab) }
            }
        }

        val mainScreen = MainScreen(
            onBack = loginRepository::logout,
            onCancel = context.eventHandler { state = state.copy(submit = false) },
            onSubmit = context.eventHandler { state = state.copy(submit = true) },
            progress = renderState.progress ?: 0f,
            processing = renderState.submit,
            selectedItem = renderState.tab,
            onItemSelected = context.eventHandler { item ->
                val isCropping = item == SheetItem.Image2Image
                if (!isCropping) settingsCache.selectedImage.tryEmit(null)
                state = state.copy(tab = item, isCropping = isCropping)
            },
            screen = context.renderChild(t2iWorkflow, Unit, handler = { noAction() }),
            settingsScreen = context.renderChild(settingsWorkflow, Unit) { noAction() },
            error = renderState.error
        )

        return ComposeScreen { viewEnvironment ->
            WorkflowRendering(
                rendering = mainScreen,
                viewEnvironment = viewEnvironment
            )

            cropChild?.let {
                WorkflowRendering(
                    rendering = cropChild,
                    viewEnvironment = viewEnvironment
                )
            }
        }
    }

    override fun snapshotState(state: State): Snapshot? = null
}