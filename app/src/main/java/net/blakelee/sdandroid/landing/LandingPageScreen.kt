package net.blakelee.sdandroid.landing

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import net.blakelee.sdandroid.text2image.ElevatedTextField
import net.blakelee.sdandroid.ui.theme.padding

data class LandingPageScreen(
    val onLogin: (url: String, username: String, password: String) -> Unit
) : ComposeScreen {

    @Composable
    override fun Content(viewEnvironment: ViewEnvironment) {
        Scaffold { paddingValues ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(padding)
            ) {
                var url by remember { mutableStateOf("") }
                PrefilledUrlTextField(
                    value = url,
                    onValueChange = { value -> url = value },
                    modifier = Modifier.fillMaxWidth()
                )

                var isChecked by remember { mutableStateOf(false) }
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                AuthFields(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it }
                )

                Button(
                    onClick = { onLogin(url, username, password) },
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        Text("Login")
                    }
                )
            }
        }
    }
}

@Composable
fun AuthFields(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    Row {

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Text(
            text = "Use authentication",
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }

    if (checked) {
        ElevatedTextField(
            value = username,
            onValueChange = onUsernameChange,
            onSubmit = {},
            hint = "Username",
            modifier = Modifier.fillMaxWidth()
        )

        ElevatedTextField(
            value = password,
            onValueChange = onPasswordChange,
            onSubmit = {},
            hint = "Password",
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PrefilledUrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val isOnlyBaseUrl = value.all { it.isLetterOrDigit() }

    val leading: @Composable (() -> Unit)? = if (isOnlyBaseUrl) {
        @Composable { Text(text = "https://", color = color) }
    } else null
    val trailing: @Composable (() -> Unit)? = if (isOnlyBaseUrl) {
        @Composable { Text(text = ".gradio.app", color = color) }
    } else null

    val cornerShape = remember { RoundedCornerShape(4.dp) }
    val focusRequester = remember { FocusRequester() }

    var showBorder by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = Color.Black.takeIf { showBorder } ?: Color.Transparent
    )

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .border(1.dp, borderColor, cornerShape)
            .width(IntrinsicSize.Max)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
            ) { focusRequester.requestFocus() }
            .onFocusChanged { focusState -> showBorder = focusState.hasFocus }

    ) {
        Row(modifier = Modifier.padding(padding)) {
            leading?.invoke()

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = LocalTextStyle.current
            )

            trailing?.invoke()
        }
    }
}