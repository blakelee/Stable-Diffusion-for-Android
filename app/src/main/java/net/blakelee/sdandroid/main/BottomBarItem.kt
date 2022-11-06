package net.blakelee.sdandroid.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ramcosta.composedestinations.spec.Route
import net.blakelee.sdandroid.R
import net.blakelee.sdandroid.destinations.Image2ImageScreenDestination
import net.blakelee.sdandroid.destinations.Text2ImageScreenDestination

enum class BottomBarItem(
    val route: Route,
    @DrawableRes val icon: Int,
    @StringRes val label: Int
) {
    Text2Image(Text2ImageScreenDestination, R.drawable.ic_text, R.string.text),
    Image2Image(Image2ImageScreenDestination, R.drawable.ic_image, R.string.text)
}