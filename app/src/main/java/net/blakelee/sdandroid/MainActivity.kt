package net.blakelee.sdandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.squareup.workflow1.WorkflowExperimentalRuntime
import com.squareup.workflow1.config.AndroidRuntimeConfigTools
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.squareup.workflow1.ui.compose.renderAsState
import dagger.hilt.android.AndroidEntryPoint
import net.blakelee.sdandroid.compose.LoadingScreenOverlayDialogFactory
import net.blakelee.sdandroid.ui.theme.SDAndroidTheme
import javax.inject.Inject

@OptIn(WorkflowExperimentalRuntime::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootWorkflow: RootWorkflow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SDAndroidTheme {

                val rendering by rootWorkflow.renderAsState(
                    props = Unit,
                    runtimeConfig = AndroidRuntimeConfigTools.getAppWorkflowRuntimeConfig(),
                    onOutput = {}
                )

                WorkflowRendering(
                    rendering = rendering,
                    viewEnvironment = ViewEnvironment.EMPTY + (
                            ViewRegistry to ViewRegistry(LoadingScreenOverlayDialogFactory.default)
                            )
                )
            }
        }
    }
}