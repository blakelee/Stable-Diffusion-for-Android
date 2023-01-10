package net.blakelee.sdandroid.settings

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.Tuple11
import net.blakelee.sdandroid.combine
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.settings.SettingsWorkflow.State
import javax.inject.Inject

class SettingsWorkflow @Inject constructor(
    loginRepository: LoginRepository,
    private val settingsCache: SettingsCache
) : StatefulWorkflow<SharedSettings?, State, Unit, ComposeScreen>() {

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
        val batchCount: Int = 1,
        val batchSize: Int = 1,
        val actionName: String? = null,
        val action: (suspend SettingsCache.() -> Unit)? = null
    )

    override fun render(
        renderProps: SharedSettings?,
        renderState: State,
        context: RenderContext
    ): ComposeScreen {

        context.runningWorker(state) {
            val (url, model, models, sampler, samplers, cfg, steps, width, height, batchCount, batchSize) = it
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
                    height = height,
                    batchCount = batchCount,
                    batchSize = batchSize
                )
            }
        }

        renderState.action?.let {
            context.runningSideEffect(it.hashCode().toString()) {
                it.invoke(settingsCache)
                context.eventHandler { state = state.copy(actionName = null, action = null) }()
            }
        }

        fun <T> updateAction(
            value: suspend SettingsCache.(T) -> Unit,
            name: String? = null
        ): (T) -> Unit =
            context.eventHandler { t ->
                state = state.copy(actionName = name, action = { value(t) })
            }

        return SettingsScreen(
            url = renderState.url,
            sampler = renderState.sampler,
            onSamplerChanged = updateAction(SettingsCache::setSampler, "sampler"),
            samplers = renderState.samplers,
            samplersEnabled = renderState.actionName != "sampler",
            model = renderState.model,
            models = renderState.models,
            modelsEnabled = renderState.actionName != "model",
            onModelChanged = updateAction(SettingsCache::setModel, "model"),
            cfg = renderState.cfg,
            onCfgChanged = updateAction(SettingsCache::setCfg),
            steps = renderState.steps,
            onStepsChanged = updateAction(SettingsCache::setSteps),
            width = renderState.width,
            onWidthChanged = updateAction(SettingsCache::setWidth),
            height = renderState.height,
            onHeightChanged = updateAction(SettingsCache::setHeight),
            batchCount = renderState.batchCount,
            onBatchCountChanged = updateAction(SettingsCache::setBatchCount),
            batchSize = renderState.batchSize,
            onBatchSizeChanged = updateAction(SettingsCache::setBatchSize)
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
        settingsCache.batchCount,
        settingsCache.batchSize,
        ::Tuple11
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