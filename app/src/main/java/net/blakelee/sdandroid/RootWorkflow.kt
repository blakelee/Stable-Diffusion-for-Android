package net.blakelee.sdandroid

import com.squareup.workflow1.*
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.landing.LoginWorkflow
import javax.inject.Inject

class RootWorkflow @Inject constructor(
    private val loginRepository: LoginRepository,
    private val loginWorkflow: LoginWorkflow,
    private val primaryWorkflow: PrimaryWorkflow
) : StatefulWorkflow<Unit, RootWorkflow.State, Nothing, ComposeScreen>() {

    sealed class State {
        object LoggedOut : State()
        object LoggedIn : State()
    }

    override fun initialState(
        props: Unit,
        snapshot: Snapshot?
    ): State =
        if (runBlocking { loginRepository.isLoggedIn.first() }) State.LoggedIn else State.LoggedOut

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(loginRepository.isLoggedIn.asWorker()) { isLoggedIn ->
            action { state = nextState(isLoggedIn) }
        }

        return when (renderState) {
            State.LoggedOut -> context.renderChild(
                loginWorkflow,
                props = Unit,
                handler = { output: Boolean -> action { state = nextState(output) } }
            )
            State.LoggedIn -> context.renderChild(
                primaryWorkflow,
                props = Unit,
                handler = { noAction() }
            )
        }

    }

    private fun nextState(isLoggedIn: Boolean): State {
        return if (isLoggedIn) State.LoggedIn else State.LoggedOut
    }

    override fun snapshotState(state: State): Snapshot? = null
}