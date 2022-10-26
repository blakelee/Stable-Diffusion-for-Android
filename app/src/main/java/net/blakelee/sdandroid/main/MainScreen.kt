package net.blakelee.sdandroid.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.utils.allDestinations
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import net.blakelee.sdandroid.NavGraphs

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val navController = rememberNavController()
    val currentDestination = navController.currentDestinationAsState().value ?: NavGraphs.login

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination !in (NavGraphs.login.allDestinations),
                enter = fadeIn(),
                exit = fadeOut()
            ) { BottomBar(navController) }

        }
    ) { paddingValues ->
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            startRoute = when (viewModel.isLoggedIn) {
                true -> NavGraphs.app
                false -> NavGraphs.login
            }
        )
    }

}