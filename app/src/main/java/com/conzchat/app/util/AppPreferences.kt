package com.conzchat.app.util

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "conzchat_prefs"
    private const val KEY_BRIGHTNESS = "brightness"
    private const val KEY_THEME = "theme"
    private const val KEY_LAST_SEEN_PREFIX = "last_seen_"
    private const val KEY_LAST_VERSION_SEEN = "last_version_seen"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBrightness(context: Context): Int = prefs(context).getInt(KEY_BRIGHTNESS, 100)
    fun setBrightness(context: Context, value: Int) =
        prefs(context).edit().putInt(KEY_BRIGHTNESS, value).apply()

    fun getTheme(context: Context): String = prefs(context).getString(KEY_THEME, "conz") ?: "conz"
    fun setTheme(context: Context, theme: String) =
        prefs(context).edit().putString(KEY_THEME, theme).apply()

    fun getLastSeen(context: Context, uid: String): Long =
        prefs(context).getLong(KEY_LAST_SEEN_PREFIX + uid, 0L)

    fun setLastSeen(context: Context, uid: String, time: Long) =
        prefs(context).edit().putLong(KEY_LAST_SEEN_PREFIX + uid, time).apply()

    // String overload used for announcement tracking
    fun getLastSeen(context: Context, key: String, defaultVal: String): String =
        prefs(context).getString(KEY_LAST_SEEN_PREFIX + key, defaultVal) ?: defaultVal

    fun setLastSeen(context: Context, key: String, value: String) =
        prefs(context).edit().putString(KEY_LAST_SEEN_PREFIX + key, value).apply()

    fun getLastVersionSeen(context: Context): String =
        prefs(context).getString(KEY_LAST_VERSION_SEEN, "") ?: ""

    fun setLastVersionSeen(context: Context, version: String) =
        prefs(context).edit().putString(KEY_LAST_VERSION_SEEN, version).apply()
}
