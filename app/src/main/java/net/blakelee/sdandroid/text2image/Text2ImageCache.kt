package net.blakelee.sdandroid.text2image

import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import net.blakelee.sdandroid.persistence.SharedCache
import net.blakelee.sdandroid.persistence.SharedPreferencesKeys
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Text2ImageCache @Inject constructor(
    override val dataStore: DataStore<Preferences>
) : SharedCache(Text2ImageCache) {

    private val workingPrompt = MutableStateFlow<String?>(null)

    val prompt: Flow<String> = prompts.combine(workingPrompt) { prompts, workingPrompt ->
        val prompt = prompts.lastOrNull().orEmpty()
        workingPrompt ?: prompt
    }

    suspend fun setPrompt(prompt: String) {
        workingPrompt.emit(prompt)
    }

    val images = MutableStateFlow(emptyList<Bitmap>())

    companion object : SharedPreferencesKeys {
        override val promptKey: String = "t2i_prompt_key"
    }
}