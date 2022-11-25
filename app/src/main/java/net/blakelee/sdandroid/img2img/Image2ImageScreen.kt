package net.blakelee.sdandroid.img2img

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.compose.config
import net.blakelee.sdandroid.compose.prompt
import net.blakelee.sdandroid.compose.steps
import net.blakelee.sdandroid.ui.theme.padding

data class Image2ImageScreen(
    val prompt: String,
    val onPromptChanged: (String) -> Unit,
    val prompts: Set<String>,
    val onPromptDeleted: (String) -> Unit,
    val cfgScale: String,
    val onCfgScaleChanged: (String) -> Unit,
    val steps: String,
    val onStepsChanged: (String) -> Unit,
    val onSubmit: () -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Column(modifier = Modifier.padding(padding)) {

            prompt(
                prompts = prompts,
                onPromptDeleted = onPromptDeleted,
                value = prompt,
                onValueChange = onPromptChanged,
                modifier = Modifier.fillMaxWidth(),
                onSubmit = onSubmit
            )

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                config(
                    value = cfgScale,
                    modifier = Modifier.weight(0.5f),
                    onValueChange = { onCfgScaleChanged(it) }
                )

                Spacer(Modifier.width(8.dp))

                steps(
                    value = steps,
                    modifier = Modifier.weight(0.5f),
                    onValueChange = onStepsChanged,
                    onSubmit = onSubmit
                )
            }
        }
    }
}