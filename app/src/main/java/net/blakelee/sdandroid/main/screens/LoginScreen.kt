package net.blakelee.sdandroid.main.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import net.blakelee.sdandroid.LoginNavGraph

@LoginNavGraph
@Destination
@Composable
fun LoginScreen() {
    Text("Hello world")
}