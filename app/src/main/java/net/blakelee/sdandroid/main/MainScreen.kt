package net.blakelee.sdandroid.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ChipDefaults.ContentOpacity
import androidx.compose.material.ChipDefaults.outlinedChipColors
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import net.blakelee.sdandroid.ui.theme.padding

data class MainScreen(
    val onBack: () -> Unit,
    val onCancel: () -> Unit,
    val onSubmit: () -> Unit,
    val processing: Boolean,
    val progress: Float,
    val selectedItem: SheetItem,
    val onItemSelected: (SheetItem) -> Unit,
    val screen: ComposeScreen,
    val settingsScreen: ComposeScreen?,
    val error: String? = null
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        BackHandler(onBack = onBack)

        val scaffoldState = rememberBottomSheetScaffoldState()

        error?.let {
            Toast.makeText(LocalContext.current, it, Toast.LENGTH_SHORT).show()
        }

        BottomSheetScaffold(
            content = { paddingValues ->
                WorkflowRendering(
                    rendering = screen,
                    viewEnvironment = viewEnvironment,
                    modifier = Modifier.padding(paddingValues)
                )
            },
            sheetContent = {
                PeekContent(
                    selectedItem = selectedItem,
                    onItemSelected = onItemSelected,
                    onBack = onBack,
                    onCancel = onCancel,
                    onSubmit = onSubmit,
                    processing = processing,
                    progress = progress
                )

                settingsScreen?.let {
                    WorkflowRendering(rendering = settingsScreen, viewEnvironment = viewEnvironment)
                } ?: run {
                    // Workaround to make content height > peek height otherwise when settings
                    // fills in it will be expanded
                    Spacer(Modifier.height(1.dp))
                }
            },
            sheetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp),
            sheetShape = RoundedCornerShape(topStart = padding, topEnd = padding),
            sheetPeekHeight = 80.dp,
            scaffoldState = scaffoldState,
            modifier = Modifier.imePadding()
        )
    }
}

@Composable
fun PeekContent(
    selectedItem: SheetItem,
    onItemSelected: (SheetItem) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
    processing: Boolean,
    progress: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
    ) {
        SheetItem.values().forEach {
            val isSelected = selectedItem == it

            Chip(
                onClick = { onItemSelected(it) },
                border = if (isSelected) ChipDefaults.outlinedBorder else null,
                colors = if (isSelected) outlinedChipColors() else unselectedColors(),
                content = {
                    Image(
                        painter = painterResource(id = it.res),
                        contentDescription = null
                    )
                },
                modifier = Modifier.padding(vertical = padding)
            )
        }


        val duration = if (!processing) 0 else 350
        val progress by animateFloatAsState(
            progress,
            tween(duration, 0, LinearEasing)
        )
        val brush = loadingBrush(processing, progress)
        val focusManager = LocalFocusManager.current

        val onClick: () -> Unit = when (processing) {
            true -> onCancel
            false -> fun() {
                focusManager.clearFocus()
                onSubmit()
            }
        }

        Spacer(Modifier.weight(1f))

        androidx.compose.material3.FloatingActionButton(
            onClick = onClick,
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            modifier = Modifier
                .background(brush, FloatingActionButtonDefaults.shape)
                .align(CenterVertically)
        ) {
            val icon =
                if (processing) net.blakelee.sdandroid.R.drawable.ic_stop else net.blakelee.sdandroid.R.drawable.ic_play
            androidx.compose.material3.Icon(painterResource(id = icon), null)
        }
    }
}

@Composable
private fun unselectedColors() = outlinedChipColors(
    backgroundColor = Color.Transparent,
    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentOpacity * ContentAlpha.disabled),
)

@Composable
fun loadingBrush(processing: Boolean, progress: Float): Brush {
    return when (processing) {
        false -> SolidColor(MaterialTheme.colorScheme.tertiaryContainer)
        true ->
            Brush.horizontalGradient(
                0.0f to Color(0xFFFF8000),
                progress to Color(0xFFFF8000),
                progress to MaterialTheme.colorScheme.primaryContainer,
                1.0f to MaterialTheme.colorScheme.primaryContainer,
                startX = 0f,
                endX = Float.POSITIVE_INFINITY
            )
    }
}
