package net.blakelee.sdandroid.settings

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.Tuple9
import net.blakelee.sdandroid.combine
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.settings.SettingsWorkflow.State
import javax.inject.Inject


class SettingsWorkflow @Inject constructor(
    private val loginRepository: LoginRepository,
    private val settingsCache: SettingsCache
) : StatefulWorkflow<SharedSettings?, State, Unit, ComposeScreen>() {

    sealed class Action {
        data class UpdateModel(val model: String) : Action()
        data class UpdateSampler(val sampler: String) : Action()
        data class UpdateCfg(val cfg: Float) : Action()
        data class UpdateSteps(val steps: Int) : Action()
        data class UpdateWidth(val width: Int) : Action()
        data class UpdateHeight(val height: Int) : Action()
    }

    data class State(
        val url: String = "",
        val model: String = "",
        val models: Set<String> = emptySet(),
        val sampler: String = "",
        val samplers: Set<String> = emptySet(),
        val cfg: Float = 8.5f,
        val steps: Int = 25,
        val width: Int = 512,
        val height: Int = 512,
        val action: Action? = null
    )

    override fun render(
        renderProps: SharedSettings?,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(state) {
            val (url, model, models, sampler, samplers, cfg, steps, width, height) = it
            action {
                state = state.copy(
                    url = url,
                    model = model,
                    models = models,
                    sampler = sampler,
                    samplers = samplers,
                    cfg = cfg,
                    steps = steps,
                    width = width,
                    height = height
                )
            }
        }

        fun sideEffect(key: String, apply: suspend SettingsCache.() -> Unit) =
            context.runningSideEffect("$key+${renderState.action.hashCode()}") {
                apply.invoke(settingsCache)
            }

        when (renderState.action) {
            is Action.UpdateModel -> sideEffect("updateModel") {
                setModel(renderState.action.model)
            }
            is Action.UpdateSampler -> sideEffect("updateSampler") {
                setSampler(renderState.action.sampler)
            }
            is Action.UpdateWidth -> sideEffect("updateWidth") {
                setWidth(renderState.action.width)
            }
            is Action.UpdateHeight -> sideEffect("updateHeight") {
                setHeight(renderState.action.height)
            }
            is Action.UpdateCfg -> sideEffect("updateCfg") {
                setCfg(renderState.action.cfg)
            }
            is Action.UpdateSteps -> sideEffect("updateSteps") {
                setSteps(renderState.action.steps)
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
            cfg = renderState.cfg,
            onCfgChanged = { cfg ->
                val cfgScaleFloat = cfg.toFloatOrNull() ?: 0f
                context.eventHandler {
                    state = state.copy(action = Action.UpdateCfg(cfgScaleFloat))
                }()
            },
            steps = renderState.steps,
            onStepsChanged = { steps ->
                val stepsInt = steps.filter { it.isDigit() }.toIntOrNull() ?: 0
                context.eventHandler { state = state.copy(action = Action.UpdateSteps(stepsInt)) }()
            },
            width = renderState.width,
            onWidthChanged = {
                context.eventHandler { state = state.copy(action = Action.UpdateWidth(it)) }()
            },
            height = renderState.height,
            onHeightChanged = {
                context.eventHandler { state = state.copy(action = Action.UpdateHeight(it)) }()
            }
        )
    }

    private val state = combine(
        loginRepository.url,
        settingsCache.model,
        settingsCache.models,
        settingsCache.sampler,
        settingsCache.samplers,
        settingsCache.cfg,
        settingsCache.steps,
        settingsCache.width,
        settingsCache.height,
        ::Tuple9
    ).asWorker()

    override fun initialState(props: SharedSettings?, snapshot: Snapshot?): State = props?.let {
        State(
            sampler = it.sampler,
            width = it.width,
            height = it.height
        )
    } ?: State()

    override fun snapshotState(state: State): Snapshot? = null
}
