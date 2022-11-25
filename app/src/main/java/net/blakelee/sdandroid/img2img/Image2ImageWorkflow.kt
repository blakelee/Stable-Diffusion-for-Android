package net.blakelee.sdandroid.img2img

import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.Submit

object Image2ImageWorkflow : StatelessWorkflow<Unit, Submit, ComposeScreen>() {
    override fun render(
        renderProps: Unit,
        context: RenderContext
    ): ComposeScreen = Image2ImageScreen(
        prompt = "",
        prompts = setOf(),
        onPromptChanged = {},
        onPromptDeleted = {},
        cfgScale = "7.5",
        onCfgScaleChanged = {},
        steps = "25",
        onStepsChanged = {},
        onSubmit = {}
    )
}
