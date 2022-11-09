package net.blakelee.sdandroid.text2image

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import net.blakelee.sdandroid.persistence.mutablePreferenceOf
import javax.inject.Inject
import javax.inject.Singleton

private const val PROMPTS = "t2i_prompts"
private const val CFG_SCALE = "t2i_cfg_scale"
private const val STEPS = "t2i_steps"

@Singleton
class RealText2ImageConfig @Inject constructor(
    @ApplicationContext context: Context
) : Text2ImageConfig {
    override var prompts: Set<String> by context.mutablePreferenceOf(
        PROMPTS,
        LinkedHashSet<String>()
    )
    override var cfgScale: Float by context.mutablePreferenceOf(CFG_SCALE, 7f)
    override var steps: Int by context.mutablePreferenceOf(STEPS, 20)
}

interface Text2ImageConfig {
    var prompts: Set<String>
    var cfgScale: Float
    var steps: Int
}
