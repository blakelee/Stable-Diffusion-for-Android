package net.blakelee.sdandroid.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.R

@Composable
fun ColumnScope.prompt(
    prompts: Set<String>,
    onPromptDeleted: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                value
            )
        )
    }

    if (prompts.isEmpty()) {
        expanded = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = text,
            onValueChange = { onValueChange(it.text); text = it },
            label = { Text("Prompt") },
            trailingIcon = {
                if (prompts.isNotEmpty()) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier
                            .rotate(if (expanded) 180f else 0f)
                            .menuAnchor()
                    )
                }
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        scope.launch(Dispatchers.Main) {
                            text = text.copy(selection = TextRange(0, text.text.length))
                        }
                    }
                },
            keyboardActions = KeyboardActions(
                onGo = {
                    onSubmit()
                    focusManager.clearFocus()
                }
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            prompts.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        text = text.copy(selectionOption)
                        onValueChange(selectionOption)
                        expanded = false
                    },
                    interactionSource = MutableInteractionSource(),
                    text = {
                        Text(text = selectionOption)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onPromptDeleted(selectionOption)
                            expanded = false
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
        }
    }
}



