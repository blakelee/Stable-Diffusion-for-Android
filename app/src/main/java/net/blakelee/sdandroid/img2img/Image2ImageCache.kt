package net.blakelee.sdandroid.img2img

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import net.blakelee.sdandroid.persistence.SharedCache
import net.blakelee.sdandroid.persistence.SharedPreferencesKeys
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Image2ImageCache @Inject constructor(
    override val dataStore: DataStore<Preferences>
) : SharedCache(Image2ImageCache) {

    companion object : SharedPreferencesKeys {
        override val promptKey: String = "i2i_prompt_key"
        override val stepsKey: String = "i2i_steps_key"
        override val cfgScaleKey: String = "i2i_cfg_scale_key"
    }
}