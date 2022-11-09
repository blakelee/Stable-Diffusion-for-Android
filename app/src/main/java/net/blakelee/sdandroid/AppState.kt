package net.blakelee.sdandroid

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import net.blakelee.sdandroid.persistence.mutablePreferenceOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAppState @Inject constructor(@ApplicationContext context: Context) : AppState {
    override var processing: Boolean by mutableStateOf(false)
    override var progress: Float by mutableStateOf(0F)
    override var onCancel: () -> Unit = {}
    override var onProcess: () -> Unit = {}
    override var url: String by context.mutablePreferenceOf("url", "")
    override var sampler: String by context.mutablePreferenceOf("sampler", "Euler a")
    override var model: String by context.mutablePreferenceOf("sd_model", "")
}

interface AppState {
    var sampler: String
    var model: String
    var url: String
    var processing: Boolean
    var progress: Float
    var onProcess: () -> Unit
    var onCancel: () -> Unit
}