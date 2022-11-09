package net.blakelee.sdandroid.text2image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.AppNavGraph
import net.blakelee.sdandroid.R
import java.io.IOException

@AppNavGraph(start = true)
@Destination
@Composable
fun Text2ImageScreen(viewModel: Text2ImageViewModel) {

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Column(modifier = Modifier.padding(8.dp)) {

        prompt(
            prompts = viewModel.prompts,
            onPromptDeleted = viewModel::deletePrompt,
            value = viewModel.prompt,
            onValueChange = viewModel::prompt::set,
            modifier = Modifier.fillMaxWidth(),
            onSubmit = viewModel::submit,
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            config(
                value = viewModel.cfgScale.toString(),
                modifier = Modifier.weight(0.5f),
                onValueChange = remember { { viewModel.setConfigurationScale(it.toFloat()) } }
            )

            Spacer(Modifier.width(8.dp))

            steps(
                value = viewModel.steps.toString(),
                modifier = Modifier.weight(0.5f),
                onValueChange = remember {
                    {
                        viewModel.steps = it.filter { it.isDigit() }.toIntOrNull() ?: 0
                    }
                },
                onSubmit = viewModel::submit
            )
        }

        viewModel.images.forEach {
            renderImage(bitmap = it, modifier = Modifier.fillMaxWidth())
        }
    }
}

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

    var text by remember { mutableStateOf(TextFieldValue(value)) }

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

@Composable
fun RowScope.config(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    val options = (0 until 30).map { (it / 2f + 1f).toString() }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = value,
            onValueChange = { },
            label = { Text("Cfg") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    },
                    interactionSource = MutableInteractionSource(),
                    text = {
                        Text(text = selectionOption)
                    })
            }
        }
    }
}

@Composable
fun RowScope.steps(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        modifier = modifier,
        label = { Text("Steps") }
    )
}

@Composable
fun ColumnScope.renderImage(bitmap: Bitmap, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var touchPoint: Offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    touchPoint = it
                    expanded = true
                }

            }
    ) {
        val (xDp, yDp) = with(density) {
            (touchPoint.x.toDp()) to (touchPoint.y.toDp())
        }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = modifier
        )

        var height by remember {
            mutableStateOf(0.dp)
        }

        DropdownMenu(
            expanded = expanded,
            offset = DpOffset(xDp, -maxHeight + yDp + height),
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.onGloballyPositioned {
                height = with(density) { it.size.height.toDp() / 0.75f }
            }
        ) {

            val context = LocalContext.current

            DropdownMenuItem(
                onClick = {
                    val uri = bitmap.toUri(context, displayName = "test")
                    expanded = false
                },
                interactionSource = MutableInteractionSource(),
                text = {
                    Text("Save Image")
                }
            )
        }
    }
}

fun Bitmap.toUri(
    context: Context,
    displayName: String
): Uri {

    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/bmp")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    var uri: Uri? = null

    return runCatching {
        with(context.contentResolver) {
            insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                uri = it // Keep uri reference so it can be removed on failure

                openOutputStream(it)?.use { stream ->
                    if (!compress(Bitmap.CompressFormat.PNG, 100, stream))
                        throw IOException("Failed to save bitmap.")
                } ?: throw IOException("Failed to open output stream.")

            } ?: throw IOException("Failed to create new MediaStore record.")
        }
    }.getOrElse {
        uri?.let { orphanUri ->
            // Don't leave an orphan entry in the MediaStore
            context.contentResolver.delete(orphanUri, null, null)
        }

        throw it
    }
}