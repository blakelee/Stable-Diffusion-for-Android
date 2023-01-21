package net.blakelee.sdandroid.settings

import com.squareup.workflow1.*
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import net.blakelee.sdandroid.landing.LoginRepository
import net.blakelee.sdandroid.settings.SettingsWorkflow.State
import javax.inject.Inject

class SettingsWorkflow @Inject constructor(
    private val loginRepository: LoginRepository,
    private val settingsCache: SettingsCache
) : StatefulWorkflow<Unit, State?, Unit, ComposeScreen?>() {

    private val updateAction = MutableSharedFlow<suspend () -> Unit>()

    data class State(
        val url: String = "",
        val model: String = "",
        val models: Set<String> = emptySet(),
        val sampler: String = "",
        val samplers: Set<String> = emptySet(),
        val denoisingStrength: Float = 0.5f,
        val cfg: Float = 8.5f,
        val steps: Int = 25,
        val width: Int = 512,
        val height: Int = 512,
        val batchCount: Int = 1,
        val batchSize: Int = 1,
        val actionName: String? = null
    )

    override fun render(
        renderProps: Unit,
        renderState: State?,
        context: RenderContext
    ): ComposeScreen? {

        context.runningWorker(Worker.fromNullable {
            State(
                url = loginRepository.url.first(),
                cfg = settingsCache.cfg.first(),
                steps = settingsCache.steps.first(),
                denoisingStrength = settingsCache.denoisingStrength.first(),
                width = settingsCache.width.first(),
                height = settingsCache.height.first(),
                batchCount = settingsCache.batchCount.first(),
                batchSize = settingsCache.batchSize.first()
            )
        }) { action { state = it } }


        context.runningWorker(
            updateAction
                .onEach { it.invoke() }
                .map { Unit }
                .asWorker()
        ) { WorkflowAction.noAction() }

        if (renderState == null) return null

        return SettingsScreen(
            url = renderState.url,
            sampler = renderState.sampler,
            onSamplerChanged = context.updateAction(SettingsCache::setSampler) {
                state = state?.copy(sampler = it, actionName = "sampler")
            },
            samplers = renderState.samplers,
            samplersEnabled = renderState.actionName != "sampler",
            model = renderState.model,
            models = renderState.models,
            modelsEnabled = renderState.actionName != "model",
            onModelChanged = context.updateAction(SettingsCache::setModel) {
                state = state?.copy(model = it, actionName = "model")
            },
            cfg = renderState.cfg,
            onCfgChanged = context.updateAction(SettingsCache::setCfg) {
                state = state?.copy(cfg = it)
            },
            steps = renderState.steps,
            onStepsChanged = context.updateAction(SettingsCache::setSteps) {
                state = state?.copy(steps = it)
            },
            width = renderState.width,
            onWidthChanged = context.updateAction(SettingsCache::setWidth) {
                state = state?.copy(width = it)
            },
            height = renderState.height,
            onHeightChanged = context.updateAction(SettingsCache::setHeight) {
                state = state?.copy(height = it)
            },
            batchCount = renderState.batchCount,
            onBatchCountChanged = context.updateAction(SettingsCache::setBatchCount) {
                state = state?.copy(batchCount = it)
            },
            batchSize = renderState.batchSize,
            onBatchSizeChanged = context.updateAction(SettingsCache::setBatchSize) {
                state = state?.copy(batchSize = it)
            }
        )
    }

    private fun <T : Any> RenderContext.updateAction(
        value: suspend SettingsCache.(T) -> Unit,
        action: WorkflowAction<*, State?, *>.Updater.(T) -> Unit
    ): (T) -> Unit {
        return eventHandler { t ->
            val cacheAction: suspend () -> Unit = {
                value.invoke(settingsCache, t)
            }
            state = state?.copy(actionName = null)
            updateAction.tryEmit(cacheAction)
            action(t)
        }
    }

    override fun initialState(props: Unit, snapshot: Snapshot?): State? = null

    override fun snapshotState(state: State?): Snapshot? = null
}