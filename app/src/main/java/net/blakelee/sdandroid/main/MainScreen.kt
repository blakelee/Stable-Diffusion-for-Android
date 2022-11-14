package net.blakelee.sdandroid.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering

data class MainScreen(
    val onBack: () -> Unit,
    val onCancel: () -> Unit,
    val onSubmit: () -> Unit,
    val processing: Boolean,
    val progress: Float,
    val selectedItem: BottomBarItem,
    val onItemSelected: (BottomBarItem) -> Unit,
    val screen: ComposeScreen
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        BackHandler(onBack = onBack)

        Scaffold(
            bottomBar = {
                WorkflowRendering(
                    rendering = BottomBar(
                        onBack = onBack,
                        onItemSelected = onItemSelected,
                        selectedItem = selectedItem,
                        onCancel = onCancel,
                        onSubmit = onSubmit,
                        processing = processing,
                        progress = progress
                    ),
                    viewEnvironment = viewEnvironment
                )
            },
            modifier = Modifier.imePadding(),
            content = { paddingValues ->
                WorkflowRendering(
                    rendering = screen,
                    viewEnvironment = viewEnvironment,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        )
    }
}