package net.blakelee.sdandroid.img2img

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import net.blakelee.sdandroid.persistence.mutablePreferenceOf
import javax.inject.Inject
import javax.inject.Singleton

private val PROMPT = "i2i_prompt"
private val CFG_SCALE = "i2i_cfg_scale"
private val DENOISING = "i2i_denoising_strength"

@Singleton
class RealImage2ImageConfig @Inject constructor(
    @ApplicationContext context: Context
) : Image2ImageConfig {
    override var prompt: String by context.mutablePreferenceOf(PROMPT, "")
    override var cfgScale: Float by context.mutablePreferenceOf(CFG_SCALE, 7f)
    override var denoisingStrength: Float by context.mutablePreferenceOf(DENOISING, 0.75f)
}

interface Image2ImageConfig {
    var prompt: String
    var cfgScale: Float
    var denoisingStrength: Float
}
