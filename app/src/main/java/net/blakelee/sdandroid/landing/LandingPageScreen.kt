package net.blakelee.sdandroid.landing

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            onValueChange = { onUsernameChange(it) },
            onSubmit = {},
            hint = "Username",
            modifier = Modifier.fillMaxWidth()
        )

        ElevatedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
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
    val interactionSource = remember { MutableInteractionSource() }
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val isOnlyBaseUrl = value.all { it.isLetterOrDigit() }

    val leading: @Composable (() -> Unit)? = if (isOnlyBaseUrl) {
        @Composable {
            Text(
                "https://",
                color = color,
                modifier = Modifier.padding(start = 16.dp, bottom = 0.5.dp, end = 0.dp)
            )
        }
    } else null
    val trailing: @Composable (() -> Unit)? = if (isOnlyBaseUrl) {
        @Composable {
            Text(
                ".gradio.app",
                color = color,
                modifier = Modifier.padding(end = 16.dp, bottom = 0.5.dp, start = 0.dp)
            )
        }
    } else null

    TextField(
        value = value,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        colors = TextFieldDefaults.textFieldColors(),
        modifier = modifier,
        leadingIcon = leading,
        trailingIcon = trailing,
        singleLine = true
    )
}