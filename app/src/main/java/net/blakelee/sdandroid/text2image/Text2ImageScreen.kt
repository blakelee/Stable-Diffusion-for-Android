package net.blakelee.sdandroid.text2image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.blakelee.sdandroid.R
import net.blakelee.sdandroid.ui.theme.padding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Text2ImageScreen(
    val prompt: String,
    val onPromptChanged: (String) -> Unit,
    val prompts: Set<String>,
    val onPromptDeleted: (String) -> Unit,
    val onSubmit: () -> Unit,
    val images: List<Bitmap>
) : ComposeScreen {

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {

            ElevatedTextField(
                value = prompt,
                onValueChange = onPromptChanged,
                hint = "Prompt",
                values = prompts,
                onValueDeleted = onPromptDeleted,
                onSubmit = onSubmit,
                modifier = Modifier.fillMaxWidth()
            )

            val pagerState = rememberPagerState()
            val scope = rememberCoroutineScope()

            HorizontalPager(count = images.size, state = pagerState) { pager ->
                renderImage(bitmap = images[pager], modifier = Modifier.fillMaxWidth())
            }

            if (images.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        content = { Text("Back") },
                        enabled = pagerState.currentPage > 0,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    )
                    Button(
                        content = { Text("Next") },
                        enabled = pagerState.currentPage < images.size - 1,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun renderImage(bitmap: Bitmap, modifier: Modifier = Modifier) {
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
                    val date = SimpleDateFormat("YYYYMMDD-HHMM", Locale.getDefault())
                        .format(Date())

                    val uri = bitmap.toUri(context, displayName = "img-$date")
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

@Composable
fun ElevatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    values: Set<String>,
    onValueDeleted: (String) -> Unit,
    onSubmit: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver, key = value) {
        mutableStateOf(TextFieldValue(value))
    }

    val cornerShape = remember { RoundedCornerShape(4.dp) }
    var showBorder by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = Color.Black.takeIf { showBorder } ?: Color.Transparent
    )

    if (values.isEmpty()) {
        expanded = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Card(
            shape = cornerShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .border(1.dp, borderColor, cornerShape)
                .width(IntrinsicSize.Max)
                .onFocusChanged { focusState -> showBorder = focusState.hasFocus }
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(24.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.labelLarge
                                .copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
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
                }

                if (values.isNotEmpty()) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier
                            .rotate(if (expanded) 180f else 0f)
                            .menuAnchor()
                    )
                }
            }
        }

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
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onValueDeleted(selectionOption)
//                            expanded = false
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