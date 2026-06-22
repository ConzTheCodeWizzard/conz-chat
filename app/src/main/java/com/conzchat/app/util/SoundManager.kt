package com.conzchat.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<String, Int>()
    private var loaded = false

    fun init(context: Context) {
        if (loaded) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
        val res = context.resources
        val pkgName = context.packageName
        listOf("msg_sent", "msg_received", "notification", "reaction_pop", "vibe_sync").forEach { name ->
            val resId = res.getIdentifier(name, "raw", pkgName)
            if (resId != 0) soundIds[name] = soundPool!!.load(context, resId, 1)
        }
        loaded = true
    }

    fun play(context: Context, name: String) {
        if (!loaded) init(context)
        soundIds[name]?.let { soundPool?.play(it, 1f, 1f, 1, 0, 1f) }
    }

    fun playNamed(context: Context, soundName: String) {
        val mapped = when (soundName) {
            "pop" -> "reaction_pop"
            "buzz" -> "msg_received"
            "chime" -> "notification"
            else -> "msg_sent"
        }
        play(context, mapped)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        loaded = false
    }
}
