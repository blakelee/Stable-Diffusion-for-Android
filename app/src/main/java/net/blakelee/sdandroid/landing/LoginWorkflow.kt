package net.blakelee.sdandroid.landing

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.compose.ComposeScreen
import javax.inject.Inject

class LoginWorkflow @Inject constructor(
    private val loginRepository: LoginRepository
) :
    StatefulWorkflow<Unit, LoginWorkflow.State, Boolean, ComposeScreen>() {

    enum class State {
        LoggedOut,
        Credentials
    }

    val login = fun(url: String) = action {
        loginRepository.login(url)
    }

    override fun initialState(
        props: Unit,
        snapshot: Snapshot?
    ): State = State.LoggedOut

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen = LandingPageScreen(context.eventHandler { url -> login(url) })

    override fun snapshotState(state: State): Snapshot? = null
}