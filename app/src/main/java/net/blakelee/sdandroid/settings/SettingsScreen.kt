package net.blakelee.sdandroid.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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

data class SettingsScreen(
    val url: String,
    val sampler: String,
    val onSamplerChanged: (String) -> Unit,
    val samplers: List<String>,
    val model: String,
    val models: List<String>,
    val onModelChanged: (String) -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Column(Modifier.padding(8.dp)) {
            val space: @Composable () -> Unit = { Spacer(Modifier.size(8.dp)) }

            SelectionContainer(modifier = Modifier.align(CenterHorizontally)) {
                Text(url)
            }

            space()

            Dropdown(
                value = sampler,
                values = samplers,
                onValueChange = onSamplerChanged,
                label = "Samplers"
            )

            space()

            Dropdown(
                value = model,
                values = models,
                onValueChange = onModelChanged,
                label = "Models"
            )
        }
    }
}

@Composable
fun Dropdown(
    value: String,
    values: List<String>,
    onValueChange: (String) -> Unit,
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