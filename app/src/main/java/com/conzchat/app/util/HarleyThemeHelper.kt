package com.conzchat.app.util

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.conzchat.app.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Applies Harley Quinn theme to the entire view hierarchy.
 * Call [applyTheme] from any Fragment's onViewCreated to theme it.
 */
object HarleyThemeHelper {

    // Harley Quinn color palette
    const val HQ_PRIMARY = 0xFFE91E8C.toInt()       // Hot pink
    const val HQ_ACCENT = 0xFF00BCD4.toInt()         // Cyan/teal blue
    const val HQ_BG = 0x00000000                     // Transparent (shows activity background image)
    const val HQ_BG_CARD = 0xCC1A0A20.toInt()        // Dark purple with alpha
    const val HQ_BG_INPUT = 0xFF2A1030.toInt()       // Slightly lighter purple
    const val HQ_TOP_BAR = 0xDD12001A.toInt()        // Deep purple-black with alpha
    const val HQ_CHAT_BAR = 0xDD0A0012.toInt()       // Dark purple-black with alpha
    const val HQ_TEXT_PRIMARY = 0xFFFFFFFF.toInt()   // White
    const val HQ_TEXT_SECONDARY = 0xFFFF80CB.toInt() // Light pink
    const val HQ_BUTTON = 0xFFE91E8C.toInt()         // Hot pink button
    const val HQ_DIVIDER = 0xFF3A1A4A.toInt()        // Purple divider

    // Colors to detect and replace
    private val DARK_BG_COLORS = setOf(
        0xFF0D0D0D.toInt(), 0xFF000000.toInt(), 0xFF1A1A1A.toInt(),
        0xFF111111.toInt(), 0xFF222222.toInt(), 0xFF121212.toInt()
    )
    private val RED_COLORS = setOf(
        0xFFCC0022.toInt(), 0xFF990019.toInt(), 0xFFFF0033.toInt(),
        0xFFCC1A0A.toInt()
    )

    fun isActive(ctx: Context): Boolean = ConzMods.isHarleyQuinnTheme(ctx)

    /**
     * Main entry point — call this in onViewCreated of any fragment.
     * It makes the fragment root transparent and recursively recolors all child views.
     */
    fun applyTheme(ctx: Context, rootView: View) {
        if (!isActive(ctx)) return
        walkAndTheme(rootView, isRoot = true)
    }

    private fun walkAndTheme(view: View, isRoot: Boolean = false) {
        // Handle the view itself
        themeView(view, isRoot)

        // Recurse into children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                walkAndTheme(view.getChildAt(i), isRoot = false)
            }
        }
    }

    private fun themeView(view: View, isRoot: Boolean) {
        // Theme background
        val bg = view.background
        if (bg != null) {
            when (bg) {
                is ColorDrawable -> {
                    val color = bg.color
                    when {
                        isRoot -> view.setBackgroundColor(Color.TRANSPARENT)
                        color in DARK_BG_COLORS -> view.setBackgroundColor(Color.TRANSPARENT)
                        color in RED_COLORS -> view.setBackgroundColor(HQ_PRIMARY)
                        color == 0xFF2A2A2A.toInt() -> view.setBackgroundColor(HQ_DIVIDER)
                        color == 0xFF80000000.toInt() -> view.setBackgroundColor(0x80120020.toInt())
                    }
                }
                is GradientDrawable -> {
                    // Card-like backgrounds (bg_tab_button etc.) — make them dark purple translucent
                    try {
                        bg.setColor(HQ_BG_CARD)
                    } catch (_: Exception) {}
                }
            }
        } else if (isRoot) {
            view.setBackgroundColor(Color.TRANSPARENT)
        }

        // Theme specific view types
        when (view) {
            is FloatingActionButton -> {
                view.backgroundTintList = android.content.res.ColorStateList.valueOf(HQ_PRIMARY)
                view.setColorFilter(HQ_TEXT_PRIMARY, PorterDuff.Mode.SRC_IN)
            }
            is SwitchCompat -> {
                view.thumbTintList = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(HQ_PRIMARY, 0xFF666666.toInt())
                )
                view.trackTintList = android.content.res.ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(0x80E91E8C.toInt(), 0xFF333333.toInt())
                )
            }
            is EditText -> {
                view.setTextColor(HQ_TEXT_PRIMARY)
                view.setHintTextColor(HQ_TEXT_SECONDARY)
                val etBg = view.background
                if (etBg is ColorDrawable || etBg is GradientDrawable) {
                    view.setBackgroundColor(HQ_BG_INPUT)
                }
            }
            is Button -> {
                val btnBg = view.background
                if (btnBg is ColorDrawable) {
                    val c = btnBg.color
                    if (c in RED_COLORS) {
                        view.setBackgroundColor(HQ_PRIMARY)
                    }
                }
                view.setTextColor(HQ_TEXT_PRIMARY)
            }
            is TextView -> {
                val textColor = view.currentTextColor
                when {
                    // Red/brand text -> pink
                    textColor in RED_COLORS || textColor == 0xFFCC0022.toInt() -> view.setTextColor(HQ_PRIMARY)
                    // Grey/secondary text -> pink secondary
                    textColor == 0xFFAAAAAA.toInt() || textColor == 0xFF888888.toInt() ||
                    textColor == 0xFF666666.toInt() || textColor == 0xFF555555.toInt() -> view.setTextColor(HQ_TEXT_SECONDARY)
                    // White stays white
                    textColor == 0xFFFFFFFF.toInt() -> {} // keep
                    // Green online indicator -> cyan accent
                    textColor == 0xFF00CC66.toInt() || textColor == 0xFF00FF88.toInt() -> view.setTextColor(HQ_ACCENT)
                    // Gold -> keep gold
                    textColor == 0xFFFFD700.toInt() -> {}
                }

                // Also check backgroundTint for buttons styled as TextViews
                val tvBg = view.background
                if (tvBg is GradientDrawable) {
                    // This is likely a styled button (like notification sound buttons)
                    // Leave it, the card theming handles it
                } else if (tvBg is ColorDrawable) {
                    val c = tvBg.color
                    if (c in RED_COLORS) {
                        view.setBackgroundColor(HQ_PRIMARY)
                    }
                }
            }
            is ImageView -> {
                // Tint icons that use the red/brand color
                if (view.imageTintList != null) {
                    val tintColor = view.imageTintList?.defaultColor ?: 0
                    if (tintColor in RED_COLORS) {
                        view.setColorFilter(HQ_PRIMARY, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
            is CardView -> {
                view.setCardBackgroundColor(HQ_BG_CARD)
            }
        }
    }
}
