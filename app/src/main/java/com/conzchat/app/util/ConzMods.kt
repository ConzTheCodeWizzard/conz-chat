package com.conzchat.app.util

import android.content.Context
import android.content.SharedPreferences

object ConzMods {
    private const val PREFS_NAME = "conz_mods"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Read Receipts
    fun isDisableReceipts(ctx: Context) = prefs(ctx).getBoolean("disable_receipts", false)
    fun setDisableReceipts(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("disable_receipts", v).apply()

    // Typing Indicator
    fun isDisableTyping(ctx: Context) = prefs(ctx).getBoolean("disable_typing", false)
    fun setDisableTyping(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("disable_typing", v).apply()

    // Fake Camera
    fun isFakeCamera(ctx: Context) = prefs(ctx).getBoolean("fake_camera", false)
    fun setFakeCamera(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("fake_camera", v).apply()

    // Screenshot Protection
    fun isScreenshotProtection(ctx: Context) = prefs(ctx).getBoolean("screenshot_protection", false)
    fun setScreenshotProtection(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("screenshot_protection", v).apply()

    // Light Mode
    fun isLightMode(ctx: Context) = prefs(ctx).getBoolean("light_mode", false)
    fun setLightMode(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("light_mode", v).apply()

    // App Lock
    fun isAppLockEnabled(ctx: Context) = prefs(ctx).getBoolean("app_lock", false)
    fun setAppLockEnabled(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("app_lock", v).apply()
    fun getAppLockPassword(ctx: Context): String = prefs(ctx).getString("app_lock_password", "") ?: ""
    fun setAppLockPassword(ctx: Context, pw: String) = prefs(ctx).edit().putString("app_lock_password", pw).apply()
    fun isAppUnlocked(ctx: Context) = prefs(ctx).getBoolean("app_unlocked", false)
    fun setAppUnlocked(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("app_unlocked", v).apply()

    // Ghost Mode
    fun isGhostMode(ctx: Context) = prefs(ctx).getBoolean("ghost_mode", false)
    fun setGhostMode(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("ghost_mode", v).apply()

    // Auto Reply
    fun isAutoReply(ctx: Context) = prefs(ctx).getBoolean("auto_reply_enabled", false)
    fun setAutoReply(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("auto_reply_enabled", v).apply()
    fun getAutoReplyMsg(ctx: Context): String = prefs(ctx).getString("auto_reply_msg", "I'm busy right now, I'll reply soon!") ?: "I'm busy right now, I'll reply soon!"
    fun setAutoReplyMsg(ctx: Context, msg: String) = prefs(ctx).edit().putString("auto_reply_msg", msg).apply()

    // Font
    fun getFont(ctx: Context): String = prefs(ctx).getString("chat_font", "default") ?: "default"
    fun setFont(ctx: Context, font: String) = prefs(ctx).edit().putString("chat_font", font).apply()

    // Bubble Style
    fun getBubbleStyle(ctx: Context): String = prefs(ctx).getString("bubble_style", "rounded") ?: "rounded"
    fun setBubbleStyle(ctx: Context, style: String) = prefs(ctx).edit().putString("bubble_style", style).apply()

    // Notification Sound
    fun getNotifSound(ctx: Context): String = prefs(ctx).getString("notif_sound", "default") ?: "default"
    fun setNotifSound(ctx: Context, sound: String) = prefs(ctx).edit().putString("notif_sound", sound).apply()

    // Mood Status
    fun getMood(ctx: Context): String = prefs(ctx).getString("mood_status", "") ?: ""
    fun setMood(ctx: Context, mood: String) = prefs(ctx).edit().putString("mood_status", mood).apply()

    // Vibe Sync
    fun isVibeSync(ctx: Context) = prefs(ctx).getBoolean("vibe_sync", false)
    fun setVibeSync(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("vibe_sync", v).apply()

    // Translate
    fun isTranslate(ctx: Context) = prefs(ctx).getBoolean("translate_enabled", false)
    fun setTranslate(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("translate_enabled", v).apply()
    fun getTranslateLang(ctx: Context): String = prefs(ctx).getString("translate_lang", "en") ?: "en"
    fun setTranslateLang(ctx: Context, lang: String) = prefs(ctx).edit().putString("translate_lang", lang).apply()

    // Vault
    fun isVaultEnabled(ctx: Context) = prefs(ctx).getBoolean("vault_enabled", false)
    fun setVaultEnabled(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("vault_enabled", v).apply()
    fun getVaultPin(ctx: Context): String = prefs(ctx).getString("vault_pin", "") ?: ""
    fun setVaultPin(ctx: Context, pin: String) = prefs(ctx).edit().putString("vault_pin", pin).apply()

    // Chat Wallpaper
    fun getChatWallpaper(ctx: Context, chatId: String): String = prefs(ctx).getString("wallpaper_$chatId", "") ?: ""
    fun setChatWallpaper(ctx: Context, chatId: String, uri: String) = prefs(ctx).edit().putString("wallpaper_$chatId", uri).apply()
    fun clearChatWallpaper(ctx: Context, chatId: String) = prefs(ctx).edit().remove("wallpaper_$chatId").apply()

    // Picture Background
    fun isPictureBgEnabled(ctx: Context) = prefs(ctx).getBoolean("picture_bg_enabled", false)
    fun setPictureBgEnabled(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("picture_bg_enabled", v).apply()
    fun getPictureBgUri(ctx: Context): String = prefs(ctx).getString("picture_bg_uri", "") ?: ""
    fun setPictureBgUri(ctx: Context, uri: String) = prefs(ctx).edit().putString("picture_bg_uri", uri).apply()

    // Self Destruct
    fun isSelfDestruct(ctx: Context) = prefs(ctx).getBoolean("self_destruct_enabled", false)
    fun setSelfDestruct(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("self_destruct_enabled", v).apply()
    fun getSelfDestructSecs(ctx: Context): Int = prefs(ctx).getInt("self_destruct_secs", 30)
    fun setSelfDestructSecs(ctx: Context, secs: Int) = prefs(ctx).edit().putInt("self_destruct_secs", secs).apply()

    // Reactions
    fun isReactionsEnabled(ctx: Context) = prefs(ctx).getBoolean("reactions_enabled", true)
    fun setReactionsEnabled(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("reactions_enabled", v).apply()

    // Sticker Pack
    fun getStickerPack(ctx: Context): String = prefs(ctx).getString("sticker_pack", "default") ?: "default"
    fun setStickerPack(ctx: Context, pack: String) = prefs(ctx).edit().putString("sticker_pack", pack).apply()

    // Voice Changer
    fun isVoiceChangerEnabled(ctx: Context) = prefs(ctx).getBoolean("voice_changer_enabled", false)
    fun setVoiceChangerEnabled(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("voice_changer_enabled", v).apply()
    fun getVoiceChangerType(ctx: Context): String = prefs(ctx).getString("voice_changer_type", "girl") ?: "girl"
    fun setVoiceChangerType(ctx: Context, type: String) = prefs(ctx).edit().putString("voice_changer_type", type).apply()
}
