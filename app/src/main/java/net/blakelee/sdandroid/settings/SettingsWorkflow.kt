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

    private val updateAction = MutableSharedFlow<suspend () -> Unit>(extraBufferCapacity = 2)

    data class State(
        val url: String = "",
        val model: String = "",
        val models: Set<String> = emptySet(),
        val sampler: String = "",
        val samplers: Set<String> = emptySet(),
        val denoisingStrength: Int = 50,
        val cfg: Float = 8.5f,
        val steps: Int = 25,
        val restoreFaces: Boolean = false,
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

        context.runningWorker(initialStateWorker()) { action { state = it } }
        context.runningWorker(actionWorker) { action { state = state?.copy(actionName = null) } }
        context.runningWorker(settingsCache.samplers.asWorker(), "samplers", ::samplers)
        context.runningWorker(settingsCache.models.asWorker(), "models", ::models)
        context.runningWorker(settingsCache.model.asWorker(), "model", ::model)
        context.runningWorker(settingsCache.sampler.asWorker(), "sampler", ::sampler)

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
            restoreFaces = renderState.restoreFaces,
            onRestoreFacesChanged = context.updateAction(SettingsCache::setRestoreFaces) {
                state = state?.copy(restoreFaces = it)
            },
            denoisingStrength = renderState.denoisingStrength,
            onDenoisingStrengthChanged = context.updateAction(SettingsCache::setDenoisingStrength) {
                state = state?.copy(denoisingStrength = it)
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

    private val actionWorker = updateAction.onEach { it.invoke() }.map { Unit }.asWorker()

    private fun sampler(sampler: String) = action {
        state = state?.copy(sampler = sampler)
    }

    private fun samplers(samplers: Set<String>) = action {
        state = state?.copy(samplers = samplers)
    }

    private fun model(model: String) = action {
        state = state?.copy(model = model)
    }

    private fun models(models: Set<String>) = action {
        state = state?.copy(models = models)
    }

    private fun initialStateWorker() = Worker.fromNullable {
        State(
            url = loginRepository.url.first(),
            cfg = settingsCache.cfg.first(),
            steps = settingsCache.steps.first(),
            denoisingStrength = settingsCache.denoisingStrength.first(),
            width = settingsCache.width.first(),
            height = settingsCache.height.first(),
            batchCount = settingsCache.batchCount.first(),
            batchSize = settingsCache.batchSize.first(),
            restoreFaces = settingsCache.restoreFaces.first()
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