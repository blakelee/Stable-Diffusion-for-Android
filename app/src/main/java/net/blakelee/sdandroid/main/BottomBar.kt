package net.blakelee.sdandroid.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import net.blakelee.sdandroid.NavGraph
import net.blakelee.sdandroid.NavGraphs
import net.blakelee.sdandroid.appDestination

@Composable
fun BottomBar(navController: NavHostController, viewModel: MainViewModel) {

    BottomAppBar(
        actions = {
            BottomBarItem.values().forEachIndexed { index, item ->
                val isCurrentDestOnBackStack = navController.isRouteOnBackStack(item.route)
                val currentDestination = navController.currentBackStackEntryAsState().value
                val isSelected = currentDestination?.appDestination() == item.route ||
                        (item.route as? NavGraph)
                            ?.destinations
                            ?.contains(currentDestination?.appDestination()) ?: false

                InputChip(
                    selected = isSelected,
                    onClick = {
                        if (isCurrentDestOnBackStack) {
                            // When we click again on a bottom bar item and it was already selected
                            // we want to pop the back stack until the initial destination of this bottom bar item
                            navController.popBackStack(item.route, false)
                            return@InputChip
                        }

                        navController.navigate(item.route.route) {
                            // Pop up to the root of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(NavGraphs.app) {
                                saveState = true
                                inclusive = true
                            }

                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    label = { Icon(painterResource(item.icon), null) },
                    border = InputChipDefaults.inputChipBorder(borderColor = Color.Transparent)
                )
            }
        },
        floatingActionButton = {

            val duration = if (!viewModel.processing) 0 else 350
            val progress by animateFloatAsState(
                viewModel.progress,
                tween(duration, 0, LinearEasing)
            )
            val brush = loadingBrush(viewModel.processing, progress)
            val focusManager = LocalFocusManager.current

            val onClick: () -> Unit = when (viewModel.processing) {
                true -> viewModel::cancel
                false -> fun() {
                    focusManager.clearFocus()
                    viewModel.submit()
                }
            }

            FloatingActionButton(
                onClick = onClick,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier = Modifier.background(brush, FloatingActionButtonDefaults.shape)
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        })
}

fun NavOptionsBuilder.popUpToTop(navController: NavController) {
    popUpTo(navController.currentBackStackEntry?.destination?.route ?: return) {
        inclusive = true
    }
}

@Composable
fun loadingBrush(processing: Boolean, progress: Float): Brush {
    return when (processing) {
        false -> SolidColor(MaterialTheme.colorScheme.primaryContainer)
        true ->
            Brush.horizontalGradient(
                0.0f to Color(0xFFFF8000),
                progress to Color(0xFFFF8000),
                progress to MaterialTheme.colorScheme.primaryContainer,
                1.0f to MaterialTheme.colorScheme.primaryContainer,
                startX = 0f,
                endX = Float.POSITIVE_INFINITY
            )
    }
}