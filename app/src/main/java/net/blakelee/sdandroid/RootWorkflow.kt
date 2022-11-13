package net.blakelee.sdandroid

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.landing.LoginWorkflow
import javax.inject.Inject

class RootWorkflow @Inject constructor(
    private val appState: AppState,
    val provider: WorkflowProvider,
    private val loginRepository: LoginRepository
) : StatefulWorkflow<Unit, RootWorkflow.State, Nothing, ComposeScreen>() {

    sealed class State {
        object LoggedOut : State()
        object LoggedIn : State()
    }

    override fun initialState(
        props: Unit,
        snapshot: Snapshot?
    ): State = if (appState.url.isNotBlank()) State.LoggedIn else State.LoggedOut

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(loginRepository.isLoggedIn.asWorker()) { isLoggedIn ->
            action { state = nextState(isLoggedIn) }
        }

        return when (renderState) {
            State.LoggedOut -> provider(context).renderChild(
                LoginWorkflow::class.java,
                props = Unit,
                handler = { output: Boolean ->
                    action { state = nextState(output) }
                }
            )
            State.LoggedIn -> provider(context).renderChild(
                LoginWorkflow::class.java,
                props = Unit,
                handler = { output: Boolean ->
                    action {
                        state = nextState(output)
                    }
                }
            )
        }

    }

    private fun nextState(isLoggedIn: Boolean): State {
        return if (isLoggedIn) State.LoggedIn else State.LoggedOut
    }

    override fun snapshotState(state: State): Snapshot? = null
}