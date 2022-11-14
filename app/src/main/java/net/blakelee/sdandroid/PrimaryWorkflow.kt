package net.blakelee.sdandroid

import android.graphics.Bitmap
import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import dagger.multibindings.ClassKey
import net.blakelee.sdandroid.PrimaryWorkflow.State
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.main.BottomBarItem
import net.blakelee.sdandroid.main.MainScreen
import net.blakelee.sdandroid.text2image.Text2ImageViewModel
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow
import javax.inject.Inject

@ClassKey(PrimaryWorkflow::class)
class PrimaryWorkflow @Inject constructor(
    private val workflowProvider: WorkflowProvider,
    private val loginRepository: LoginRepository,
    private val text2Image: Text2ImageViewModel
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    data class State(
        val processing: Boolean = false,
        val progress: Float = 0f,
        val selectedItem: BottomBarItem = BottomBarItem.Text2Image,
        val onSubmit: () -> Unit,
        val onCancel: () -> Unit,
        val images: List<Bitmap> = emptyList()
    )

    val nextState = fun(selectedItem: BottomBarItem) = action {
        state = state.copy(selectedItem = selectedItem)
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State =
        State(onSubmit = {}, onCancel = {})

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(text2Image.images.asWorker()) {
            action { state = state.copy(images = it) }
        }



        return MainScreen(
            onBack = { loginRepository.logout() },
            onCancel = {},
            onSubmit = renderState.onSubmit,
            progress = renderState.progress,
            processing = renderState.processing,
            selectedItem = renderState.selectedItem,
            onItemSelected = { selectedItem -> context.actionSink.send(nextState(selectedItem)) },
            screen = nextState(context, renderState)
        )
    }

    private fun nextState(
        context: BaseRenderContext<Unit, State, Unit>,
        state: State
    ): ComposeScreen {
        val render = workflowProvider(context)
        return when (state.selectedItem) {
            BottomBarItem.Text2Image -> {
                val props = Text2ImageWorkflow.Props(state.images) {
                    context.actionSink.send(submitText2Image)
                }
                render.renderChild(Text2ImageWorkflow::class.java, props)
            }
            BottomBarItem.Image2Image -> TODO()
            else -> TODO()
        }
    }

    private val submitText2Image = action {
        text2Image.submit()
        state = state.copy(
            processing = true,
            onCancel = text2Image::interrupt
        )
    }

    override fun snapshotState(state: State): Snapshot? = null
}
