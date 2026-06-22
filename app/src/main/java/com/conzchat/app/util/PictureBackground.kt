package com.conzchat.app.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

object PictureBackground {
    fun apply(context: Context, imageView: ImageView) {
        if (!ConzMods.isPictureBgEnabled(context)) {
            imageView.setImageDrawable(null)
            return
        }
        val uri = ConzMods.getPictureBgUri(context)
        if (uri.isNotEmpty()) {
            Glide.with(context).load(Uri.parse(uri)).centerCrop().into(imageView)
        }
    }
}
