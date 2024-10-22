package net.blakelee.sdandroid.main

import androidx.annotation.DrawableRes
import net.blakelee.sdandroid.R

enum class SheetItem(@DrawableRes val res: Int) {
    Text2Image(R.drawable.ic_text),
    Image2Image(R.drawable.ic_image)
}