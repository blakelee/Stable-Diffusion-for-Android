package net.blakelee.sdandroid.landing

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import net.blakelee.sdandroid.LoginNavGraph
import net.blakelee.sdandroid.NavGraphs
import net.blakelee.sdandroid.ui.theme.padding

@LoginNavGraph(start = true)
@Destination(deepLinks = [DeepLink(uriPattern = "https://{subdomain}.gradio.app")])
@Composable
fun LandingPageScreen(
    navController: NavController,
    viewModel: LandingPageViewModel = hiltViewModel(),
    subdomain: String? = null
) {

    // Auto navigate if possible
    if (viewModel.isLoggedIn) navController.navigate(NavGraphs.app)
    subdomain?.let(viewModel::login)

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(padding)
    ) {
        var url by remember { mutableStateOf("") }
        PrefilledUrlTextField(
            value = url,
            onValueChange = { value -> url = value },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.size(padding))

        Button(
            onClick = { viewModel.login(url) },
            modifier = Modifier.fillMaxWidth(),
            content = {
                Text("Login")
            }
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