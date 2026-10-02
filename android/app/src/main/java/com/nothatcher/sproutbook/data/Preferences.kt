package com.nothatcher.sproutbook.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

private val Context.store by preferencesDataStore("family_settings")

data class Preferences(
    val selected: String? = null,
    val grandparent: Boolean = false,
    val dark: Boolean = true,
    val volumeUnit: String = "mL",
    val weightUnit: String = "kg",
    val time24: Boolean = false,
    val notifications: Boolean = false,
    val reduceMotion: Boolean = true,
    val lastBackup: Long = 0,
)

class Settings(private val context: Context) {
    val flow =
        context.store.data
            .catch { if (it is java.io.IOException) emit(emptyPreferences()) else throw it }
            .map { p ->
                Preferences(
                    p[stringPreferencesKey("selected")],
                    p[booleanPreferencesKey("grandparent")] ?: false,
                    p[booleanPreferencesKey("dark")] ?: true,
                    p[stringPreferencesKey("volumeUnit")]?.takeIf { it in listOf("mL", "fl oz") }
                        ?: "mL",
                    p[stringPreferencesKey("weightUnit")]?.takeIf { it in listOf("kg", "lb") }
                        ?: "kg",
                    p[booleanPreferencesKey("time24")] ?: false,
                    p[booleanPreferencesKey("notifications")] ?: false,
                    p[booleanPreferencesKey("reduceMotion")] ?: true,
                    p[longPreferencesKey("lastBackup")] ?: 0,
                )
            }

    suspend fun select(id: String?) {
        context.store.edit { p ->
            if (id == null) p.remove(stringPreferencesKey("selected"))
            else p[stringPreferencesKey("selected")] = id
        }
    }

    suspend fun boolean(key: String, value: Boolean) {
        context.store.edit { it[booleanPreferencesKey(key)] = value }
    }

    suspend fun string(key: String, value: String) {
        context.store.edit { it[stringPreferencesKey(key)] = value }
    }

    suspend fun backupNow() {
        context.store.edit { it[longPreferencesKey("lastBackup")] = System.currentTimeMillis() }
    }
}
