package net.blakelee.sdandroid.persistence

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> Context.mutablePreferenceOf(
    key: String,
    default: T
): MutablePreference<T> = MutablePreference(this, key, default)

private const val USER_PREFERENCES_NAME = "user_preferences"

class MutablePreference<T>(context: Context, key: String, default: T) : MutableState<T> {
    private var preference by Preference(context, key, default)

    private var _value by mutableStateOf(preference)

    override var value: T = default
        get() = _value
        set(value) {
            _value = value
            preference = value
            field = value
        }

    override fun component1(): T = value
    override fun component2(): (T) -> Unit = { value = it }
}

private class Preference<T>(
    private val context: Context,
    private val name: String,
    private val default: T
) : ReadWriteProperty<Any?, T> {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(name, default)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    private fun <T> findPreference(name: String, default: T): T = with(prefs) {
        val res: Any? = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            is Set<*> -> getStringSet(name, default as Set<String>).let { result ->
                LinkedHashSet<String>().apply { addAll(result as Set<String>) }
            }
            else -> throw IllegalArgumentException("This type cannot be saved into Preferences")
        }
        @Suppress("UNCHECKED_CAST")
        res as T
    }

    @SuppressLint("CommitPrefEdits")
    private fun <T> putPreference(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            is Set<*> -> putStringSet(name, value as Set<String>)
            else -> throw IllegalArgumentException("This type cannot be saved into Preferences")
        }.apply()
    }
}