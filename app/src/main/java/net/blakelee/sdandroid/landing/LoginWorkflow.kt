package net.blakelee.sdandroid.landing

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.container.BodyAndOverlaysScreen
import net.blakelee.sdandroid.compose.LoadingOverlay
import javax.inject.Inject

class LoginWorkflow @Inject constructor(
    private val loginRepository: LoginRepository
) : StatefulWorkflow<Unit, LoginWorkflow.State, Boolean, BodyAndOverlaysScreen<*, *>>() {

    sealed class State {
        object LoggedOut : State()
        data class LoggingIn(val url: String, val username: String, val password: String) : State()
    }

    override fun initialState(
        props: Unit,
        snapshot: Snapshot?
    ): State = State.LoggedOut

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): BodyAndOverlaysScreen<ComposeScreen, *> {
        val overlay = if (renderState is State.LoggingIn) listOf(LoadingOverlay) else emptyList()

        if (renderState is State.LoggingIn) {
            context.runningWorker(renderState.login()) {
                action { state = State.LoggedOut }
            }
        }

        return BodyAndOverlaysScreen(
            LandingPageScreen(
                context.eventHandler { url, username, password ->
                    state = State.LoggingIn(url, username, password)
                }
            ),
            overlay
        )
    }

    private fun State.LoggingIn.login() = Worker.from {
        loginRepository.login(url, username, password)
    }

    override fun snapshotState(state: State): Snapshot? = null
}