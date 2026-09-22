package com.chevstrap.rbx.ui.components

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.content.ContextCompat
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import androidx.core.graphics.toColorInt

object ComponentAppearance {
    @JvmStatic
    fun styleButton(button: LinearLayout) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 30f

        val isDark = App.config.data.appThemeInApp == "dark"

        val videoUri: String = App.config.data.backgroundImageUri
        val useTransparent = videoUri.isNotEmpty()

        if (isDark) {
            if (useTransparent) {
                drawable.setColor("#66111111".toColorInt())
                drawable.setStroke(2, "#19070707".toColorInt())
            } else {
                drawable.setColor("#111111".toColorInt())
                drawable.setStroke(2, "#070707".toColorInt())
            }
        } else {
            if (useTransparent) {
                drawable.setColor("#66F0F3F5".toColorInt())
                drawable.setStroke(2, "#19EAEDEF".toColorInt())
            } else {
                drawable.setColor("#F0F3F5".toColorInt())
                drawable.setStroke(2, "#EAEDEF".toColorInt())
            }
        }

        button.background = drawable
    }

    @JvmStatic
    fun styleTextbox(textbox: LinearLayout) {
        val normal = GradientDrawable()
        normal.cornerRadius = 20f

        val videoUri: String = App.config.data.backgroundImageUri
        val useTransparent = videoUri.isNotEmpty()

        if (useTransparent) {
            if (App.config.data.appThemeInApp == "dark") {
                normal.setColor("#66060606".toColorInt())
                normal.setStroke(2, "#19202020".toColorInt())
            } else {
                normal.setColor("#19FFFFFF".toColorInt())
                normal.setStroke(2, "#66DFDFDF".toColorInt())
            }
        } else {
            if (App.config.data.appThemeInApp == "dark") {
                normal.setColor("#060606".toColorInt())
                normal.setStroke(2, "#202020".toColorInt())
            } else {
                normal.setColor("#FFFFFF".toColorInt())
                normal.setStroke(2, "#DFDFDF".toColorInt())
            }
        }

        val states = StateListDrawable()
        states.addState(intArrayOf(android.R.attr.state_enabled), normal)
        states.addState(intArrayOf(), normal)

        textbox.background = states
    }

    @JvmStatic
    fun styleDropdown(spinner: Spinner) {
        spinner.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                spinner.removeOnLayoutChangeListener(this)
                spinner.background = buildSpinnerBackground(spinner)
            }
        })
    }

    private fun buildSpinnerBackground(spinner: Spinner): Drawable {
        val gradient = GradientDrawable()
        gradient.cornerRadius = 30f

        val isDark = App.config.data.appThemeInApp == "dark"

        val videoUri: String = App.config.data.backgroundImageUri
        val useTransparent = videoUri.isNotEmpty()

        if (isDark) {
            if (useTransparent) {
                gradient.setColor("#19262626".toColorInt())
                gradient.setStroke(2, "#66252525".toColorInt())
            } else {
                gradient.setColor("#262626".toColorInt())
                gradient.setStroke(2, "#252525".toColorInt())
            }
        } else {
            if (useTransparent) {
                gradient.setColor("#19FFFFFF".toColorInt())
                gradient.setStroke(2, "#66DADADA".toColorInt())
            } else {
                gradient.setColor("#FFFFFF".toColorInt())
                gradient.setStroke(2, "#DADADA".toColorInt())
            }
        }

        val arrow = ContextCompat.getDrawable(
            spinner.context,
            R.drawable.arrow_down
        )

        if (arrow != null) {
            val arrowColor = if (isDark)
                "#FFFFFF".toColorInt()
            else
                "#000000".toColorInt()

            arrow.setTint(arrowColor)

            return buildLayerDrawable(
                spinner,
                gradient,
                arrow
            )
        }

        return gradient
    }

    private fun buildLayerDrawable(
        spinner: Spinner,
        background: Drawable?,
        arrow: Drawable
    ): LayerDrawable {
        val iconSizeDp = 24
        val density = spinner.resources.displayMetrics.density
        val iconPx = (iconSizeDp * density).toInt()
        val rightPaddingPx = (16 * density).toInt()

        val layers = arrayOf(background, arrow)
        val layerDrawable = LayerDrawable(layers)

        layerDrawable.setLayerInset(0, 0, 0, 0, 0)
        layerDrawable.setLayerGravity(1, Gravity.END or Gravity.CENTER_VERTICAL)
        layerDrawable.setLayerWidth(1, iconPx)
        layerDrawable.setLayerHeight(1, iconPx)
        layerDrawable.setLayerInsetEnd(1, rightPaddingPx)

        return layerDrawable
    }

    @JvmStatic
    fun updateDropdownAppearance(
        spinner: Spinner,
        enabled: Boolean
    ) {
        spinner.isEnabled = enabled
        spinner.alpha = if (enabled) 1.0f else 0.4f
    }

    @JvmStatic
    fun updateToggleAppearance(
        toggleSwitch: ImageView,
        state: Boolean,
        enabled: Boolean
    ) {
        if (App.config.data.appThemeInApp == "light") {
            toggleSwitch.setBackgroundResource(
                if (state)
                    R.drawable.toggle_on_light
                else
                    R.drawable.toggle_off_light
            )
        } else {
            toggleSwitch.setBackgroundResource(
                if (state)
                    R.drawable.toggle_on
                else
                    R.drawable.toggle_off
            )
        }

        toggleSwitch.tag = state
        toggleSwitch.alpha = if (enabled) 1.0f else 0.4f
    }
}