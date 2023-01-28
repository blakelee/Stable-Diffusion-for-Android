package net.blakelee.sdandroid.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SimpleDropdown(
    value: String,
    values: List<String>,
    hint: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                value
            )
        )
    }

    if (values.isEmpty()) {
        expanded = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        val rotate by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
        TextField(
            value = text,
            onValueChange = { onValueChange(it.text); text = it },
            label = { Text(hint) },
            trailingIcon = {
                if (values.isNotEmpty()) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier
                            .rotate(rotate)
                            .menuAnchor()
                    )
                }
            },
            readOnly = true,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        scope.launch(Dispatchers.Main) {
                            text = text.copy(selection = TextRange(0, text.text.length))
                        }
                    }
                }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            values.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        text = text.copy(selectionOption)
                        onValueChange(selectionOption)
                        expanded = false
                    },
                    interactionSource = MutableInteractionSource(),
                    text = {
                        Text(text = selectionOption)
                    }
                )
            }
        }
    }
}