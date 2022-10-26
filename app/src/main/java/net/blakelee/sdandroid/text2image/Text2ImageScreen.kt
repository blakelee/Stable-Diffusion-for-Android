package net.blakelee.sdandroid.text2image

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import net.blakelee.sdandroid.AppNavGraph
import net.blakelee.sdandroid.NavGraphs

@AppNavGraph(start = true)
@Destination
@Composable
fun Text2ImageScreen(
    navController: NavController,
    viewModel: Text2ImageViewModel = hiltViewModel()
) {

    BackHandler {
        viewModel.logout()
        navController.navigate(NavGraphs.login)
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

        val prompt by remember { derivedStateOf { viewModel.config.prompt } }
        TextField(
            value = prompt,
            onValueChange = { viewModel.setPrompt(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            config(viewModel, Modifier.weight(0.5f))
            Spacer(Modifier.width(8.dp))
            steps(viewModel, Modifier.weight(0.5f))
        }


        Button(
            onClick = { viewModel.submit() },
            content = { Text("Submit") },
            modifier = Modifier.fillMaxWidth()
        )

        viewModel.images?.forEach {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RowScope.config(viewModel: Text2ImageViewModel, modifier: Modifier) {
    val options = (0 until 30).map { (it / 2f + 1f).toString() }

    var expanded by remember { mutableStateOf(false) }
    val selectedOptionText by remember { derivedStateOf { viewModel.config.cfg_scale.toString() } }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedOptionText,
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
                        viewModel.setConfigurationScale(selectionOption.toFloat())
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
fun RowScope.steps(viewModel: Text2ImageViewModel, modifier: Modifier) {
    val steps by remember { derivedStateOf { viewModel.config.steps.toString() } }
    TextField(
        value = steps,
        onValueChange = { viewModel.setSteps(it.filter { it.isDigit() }.toIntOrNull() ?: 0) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        label = { Text("Steps") }
    )
}