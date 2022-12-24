package net.blakelee.sdandroid.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlin.math.roundToInt

data class SettingsScreen(
    val url: String,
    val sampler: String,
    val onSamplerChanged: (String) -> Unit,
    val samplers: Set<String>,
    val samplersEnabled: Boolean,
    val model: String,
    val models: Set<String>,
    val modelsEnabled: Boolean,
    val onModelChanged: (String) -> Unit,
    val width: Int,
    val height: Int,
    val onWidthChanged: (Int) -> Unit,
    val onHeightChanged: (Int) -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            SelectionContainer(modifier = Modifier.align(CenterHorizontally)) {
                Text(url)
            }


            Dropdown(
                value = sampler,
                values = samplers,
                onValueChange = onSamplerChanged,
                enabled = samplersEnabled,
                label = "Samplers"
            )

            Dropdown(
                value = model,
                values = models,
                onValueChange = onModelChanged,
                enabled = modelsEnabled,
                label = "Models"
            )

            DimensionSlider(
                text = { position -> "Width: $position" },
                onValueChange = onWidthChanged,
                value = width
            )

            DimensionSlider(
                text = { position -> "Height: $position" },
                onValueChange = onHeightChanged,
                value = height
            )
        }
    }
}

@Composable
fun DimensionSlider(
    text: (String) -> String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var value by remember(value) { mutableStateOf(value) }

    Text(text = text(value.toString()))
    Slider(
        value = value / 100f,
        onValueChange = { value = (it * 100).roundToInt() },
        valueRange = 2.56f..10.24f,
        steps = 11,
        onValueChangeFinished = {
            onValueChange(value)
        }
    )
}

@Composable
fun Dropdown(
    value: String,
    values: Set<String>,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    if (values.isEmpty()) {
        expanded = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = value,
            onValueChange = { },
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                if (values.isNotEmpty()) {
                    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            null,
                            Modifier.rotate(rotation)
                        )
                    }
                }
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            values.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    text = { Text(text = selectionOption) }
                )
            }
        }
    }
}