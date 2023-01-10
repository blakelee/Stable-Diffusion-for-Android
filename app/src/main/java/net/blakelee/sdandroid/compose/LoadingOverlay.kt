package net.blakelee.sdandroid.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewEnvironmentKey
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.container.ScreenOverlay
import com.squareup.workflow1.ui.container.ScreenOverlayDialogFactory
import net.blakelee.sdandroid.ui.theme.padding

object LoadingScreen : ComposeScreen {
    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Surface(
            modifier = Modifier
                .padding(padding * 2)
                .wrapContentSize()
                .clip(RoundedCornerShape(4.dp))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(padding)
                    .fillMaxWidth(0.9f)

            ) {
                Text("Loading")
                CircularProgressIndicator()
            }
        }
    }
}

object LoadingOverlay : ScreenOverlay<LoadingScreen> {
    override val content: LoadingScreen = LoadingScreen
}

class LoadingScreenOverlayDialogFactory : ScreenOverlayDialogFactory<
        LoadingScreen,
        LoadingOverlay
        >(LoadingOverlay::class) {

    companion object :
        ViewEnvironmentKey<LoadingScreenOverlayDialogFactory>(LoadingScreenOverlayDialogFactory::class) {
        override val default: LoadingScreenOverlayDialogFactory =
            LoadingScreenOverlayDialogFactory()
    }
}