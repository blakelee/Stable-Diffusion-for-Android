package net.blakelee.sdandroid.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.R

data class BottomBar(
    val onBack: () -> Unit,
    val onCancel: () -> Unit,
    val onSubmit: () -> Unit,
    val onItemSelected: (BottomBarItem) -> Unit,
    val selectedItem: BottomBarItem,
    val processing: Boolean,
    val progress: Float
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        BottomAppBar(
            actions = {
                BottomBarItem.values().forEach { item ->
                    val isSelected = selectedItem == item

                    if (item == BottomBarItem.Settings)
                        Spacer(Modifier.weight(1f, fill = true))

                    BottomBarItem(item, isSelected, { onItemSelected(item) })
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

                Spacer(Modifier.size(16.dp))

                FloatingActionButton(
                    onClick = onClick,
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    modifier = Modifier
                        .background(brush, FloatingActionButtonDefaults.shape)
                ) {
                    val icon = if (processing) R.drawable.ic_stop else R.drawable.ic_play
                    Icon(painterResource(id = icon), null)
                }
            },
            contentPadding = PaddingValues(horizontal = 16.dp)
        )
    }
}

@Composable
fun BottomBarItem(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = isSelected,
        onClick = onClick,
        label = { Icon(painterResource(item.res), null) },
        border = InputChipDefaults.inputChipBorder(borderColor = Color.Transparent),
        modifier = modifier
    )
}

@Composable
fun loadingBrush(processing: Boolean, progress: Float): Brush {
    return when (processing) {
        false -> SolidColor(MaterialTheme.colorScheme.primaryContainer)
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