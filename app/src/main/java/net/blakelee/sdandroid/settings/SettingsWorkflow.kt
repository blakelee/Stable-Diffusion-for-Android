package net.blakelee.sdandroid.settings

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.settings.SettingsWorkflow.State
import javax.inject.Inject


class SettingsWorkflow @Inject constructor(
    private val loginRepository: LoginRepository,
    private val settingsCache: SettingsCache
) : StatefulWorkflow<Unit, State, Unit, ComposeScreen>() {

    sealed class Action {
        data class UpdateModel(val model: String) : Action()
        data class UpdateSampler(val sampler: String) : Action()
    }

    data class State(
        val url: String = "",
        val model: String = "",
        val models: Set<String> = emptySet(),
        val sampler: String = "",
        val samplers: Set<String> = emptySet(),
        val action: Action? = null
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(loginRepository.url.asWorker(), "url") {
            action { state = state.copy(url = it) }
        }

        context.runningWorker(settingsCache.model.asWorker(), "model") {
            action { state = state.copy(model = it) }
        }

        context.runningWorker(settingsCache.models.asWorker(), "models") {
            action { state = state.copy(models = it) }
        }

        context.runningWorker(settingsCache.sampler.asWorker(), "sampler") {
            action { state = state.copy(sampler = it) }
        }

        context.runningWorker(settingsCache.samplers.asWorker(), "samplers") {
            action { state = state.copy(samplers = it) }
        }

        when (renderState.action) {
            is Action.UpdateModel -> context.runningSideEffect("updateModel+${renderState.action.hashCode()}") {
                settingsCache.setModel(renderState.action.model)
                context.eventHandler { state = state.copy(action = null) }

            }
            is Action.UpdateSampler -> context.runningSideEffect("updateSampler+${renderState.action.hashCode()}") {
                settingsCache.setSampler(renderState.action.sampler)
                context.eventHandler { state = state.copy(action = null) }
            }
            else -> {}
        }

        return SettingsScreen(
            url = renderState.url,
            sampler = renderState.sampler,
            onSamplerChanged = {
                context.eventHandler { state = state.copy(action = Action.UpdateSampler(it)) }()
            },
            samplers = renderState.samplers,
            samplersEnabled = renderState.action !is Action.UpdateSampler,
            model = renderState.model,
            models = renderState.models,
            modelsEnabled = renderState.action !is Action.UpdateModel,
            onModelChanged = {
                context.eventHandler { state = state.copy(action = Action.UpdateModel(it)) }()
            }
        )
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun snapshotState(state: State): Snapshot? = null
}
