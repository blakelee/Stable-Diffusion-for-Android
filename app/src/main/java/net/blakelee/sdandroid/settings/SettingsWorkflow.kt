package net.blakelee.sdandroid.settings

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.settings.SettingsWorkflow.State
import javax.inject.Inject


class SettingsWorkflow @Inject constructor(
    private val loginRepository: LoginRepository
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    data class State(
        val url: String = "",
        val model: String = "",
        val sampler: String = ""
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {
        context.runningWorker(loginRepository.url.asWorker()) {
            action {
                state = state.copy(url = it)
            }
        }

        return SettingsScreen(
            url = renderState.url,
            sampler = "",
            onSamplerChanged = {},
            samplers = listOf(),
            model = "",
            models = listOf(),
            onModelChanged = {}

        )
    }

    private fun getThings() {
//        val samplers = repository.samplers()
//
//        val models = repository.models()
//        val model = repository.options().sd_model_checkpoint
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun snapshotState(state: State): Snapshot? = null
}
