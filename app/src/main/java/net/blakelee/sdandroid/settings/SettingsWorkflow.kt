package net.blakelee.sdandroid.settings

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.Tuple7
import net.blakelee.sdandroid.combine
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
        data class UpdateWidth(val width: Int) : Action()
        data class UpdateHeight(val height: Int) : Action()
    }

    data class State(
        val url: String = "",
        val model: String = "",
        val models: Set<String> = emptySet(),
        val sampler: String = "",
        val samplers: Set<String> = emptySet(),
        val width: Int = 512,
        val height: Int = 512,
        val action: Action? = null
    )

    override fun render(
        renderProps: Unit,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(state) {
            val (url, model, models, sampler, samplers, width, height) = it
            action {
                state = state.copy(
                    url = url,
                    model = model,
                    models = models,
                    sampler = sampler,
                    samplers = samplers,
                    width = width,
                    height = height
                )
            }
        }

        when (renderState.action) {
            is Action.UpdateModel -> context.runningSideEffect("updateModel+${renderState.action.hashCode()}") {
                settingsCache.setModel(renderState.action.model)
            }
            is Action.UpdateSampler -> context.runningSideEffect("updateSampler+${renderState.action.hashCode()}") {
                settingsCache.setSampler(renderState.action.sampler)
            }
            is Action.UpdateWidth -> context.runningSideEffect("updateWidth+${renderState.action.hashCode()}") {
                settingsCache.setWidth(renderState.action.width)
            }
            is Action.UpdateHeight -> context.runningSideEffect("updateHeight+${renderState.action.hashCode()}") {
                settingsCache.setHeight(renderState.action.height)
            }
            null -> {}
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
            },
            height = renderState.height,
            width = renderState.width,
            onHeightChanged = {
                context.eventHandler { state = state.copy(action = Action.UpdateHeight(it)) }()
            },
            onWidthChanged = {
                context.eventHandler { state = state.copy(action = Action.UpdateWidth(it)) }()
            }
        )
    }

    private val state = combine(
        loginRepository.url,
        settingsCache.model,
        settingsCache.models,
        settingsCache.sampler,
        settingsCache.samplers,
        settingsCache.width,
        settingsCache.height,
        ::Tuple7
    ).asWorker()

    override fun initialState(props: Unit, snapshot: Snapshot?): State = State()

    override fun snapshotState(state: State): Snapshot? = null
}
