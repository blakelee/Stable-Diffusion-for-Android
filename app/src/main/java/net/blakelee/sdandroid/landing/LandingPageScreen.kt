package net.blakelee.sdandroid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import net.blakelee.sdandroid.landing.LandingPageViewModel

@LoginNavGraph(start = true)
@Destination
@Composable
fun LandingPageScreen(
    navController: NavController,
    viewModel: LandingPageViewModel = hiltViewModel()
) {

    val isLoggedIn by viewModel.isLoggedIn.collectAsState(false)

    if (isLoggedIn) {
        navController.navigate(NavGraphs.app)
    }

    Column(verticalArrangement = Arrangement.Center) {
        var url by remember { mutableStateOf("") }
        TextField(
            value = url,
            onValueChange = { value -> url = value },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.login(url) },
            modifier = Modifier.fillMaxWidth(),
            content = {
                Text("Login")
            })
    }

}