package net.blakelee.sdandroid.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.allDestinations
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import net.blakelee.sdandroid.MainActivity
import net.blakelee.sdandroid.NavGraphs
import net.blakelee.sdandroid.destinations.LandingPageScreenDestination

@Composable
fun MainScreen(activity: MainActivity, viewModel: MainViewModel = hiltViewModel(activity)) {

    val navController = rememberNavController()
    val currentDestination = navController.currentDestinationAsState().value ?: NavGraphs.login

    if (currentDestination in NavGraphs.app.nestedNavGraphs && !viewModel.isLoggedIn) {
        navController.navigate(LandingPageScreenDestination())
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination !in (NavGraphs.login.allDestinations),
                enter = fadeIn(),
                exit = fadeOut()
            ) { BottomBar(navController, viewModel) }

        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            startRoute = when (viewModel.isLoggedIn) {
                true -> NavGraphs.app
                false -> NavGraphs.login
            },
            dependenciesContainerBuilder = {
                dependency(hiltViewModel<MainViewModel>(activity))
            }
        )
    }

}