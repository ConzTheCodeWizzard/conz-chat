package com.conzchat.app.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.isVisible() = visibility == View.VISIBLE

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context.toastLong(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun ImageView.loadAvatar(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_default_avatar)
    } else {
        Glide.with(this)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_default_avatar)
            .error(R.drawable.ic_default_avatar)
            .into(this)
    }
}

fun ImageView.loadImage(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_placeholder)
    } else {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_placeholder)
            .into(this)
    }
}

fun String.truncate(maxLen: Int): String {
    return if (length > maxLen) substring(0, maxLen) + "…" else this
}
