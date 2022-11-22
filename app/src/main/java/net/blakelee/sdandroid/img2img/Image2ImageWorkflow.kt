package net.blakelee.sdandroid.img2img

import com.squareup.workflow1.StatelessWorkflow
import com.squareup.workflow1.ui.compose.ComposeScreen

object Image2ImageWorkflow : StatelessWorkflow<Unit, Unit, ComposeScreen>() {
    override fun render(
        renderProps: Unit,
        context: RenderContext
    ): ComposeScreen = Image2ImageScreen
}
