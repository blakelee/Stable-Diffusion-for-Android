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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.compose.config
import net.blakelee.sdandroid.compose.prompt
import net.blakelee.sdandroid.compose.steps
import net.blakelee.sdandroid.ui.theme.padding
import java.io.IOException

class Text2ImageScreen(
    val prompt: String,
    val onPromptChanged: (String) -> Unit,
    val prompts: Set<String>,
    val onPromptDeleted: (String) -> Unit,
    val onSubmit: () -> Unit,
    val cfgScale: String,
    val onCfgScaleChanged: (String) -> Unit,
    val steps: String,
    val onStepsChanged: (String) -> Unit,
    val images: List<Bitmap>
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Column(modifier = Modifier.padding(padding)) {

            prompt(
                prompts = prompts,
                onPromptDeleted = onPromptDeleted,
                value = prompt,
                onValueChange = onPromptChanged,
                modifier = Modifier.fillMaxWidth(),
                onSubmit = onSubmit
            )

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                config(
                    value = cfgScale,
                    modifier = Modifier.weight(0.5f),
                    onValueChange = { onCfgScaleChanged(it) }
                )

                Spacer(Modifier.width(8.dp))

                steps(
                    value = steps,
                    modifier = Modifier.weight(0.5f),
                    onValueChange = onStepsChanged,
                    onSubmit = onSubmit
                )
            }

            images.forEach {
                renderImage(bitmap = it, modifier = Modifier.fillMaxWidth())
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