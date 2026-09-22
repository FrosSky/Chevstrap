package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.ui.components.holders.SmallButtonResult

object SmallButtonBuilder {
    fun addSmallButton(
        context: Context?,
        name: String?,
        parent: LinearLayout?
    ): SmallButtonResult {
        val inflater = LayoutInflater.from(context)

        val buttonView = inflater.inflate(
            R.layout.custom_button_very_small,
            parent,
            false
        )

        val container =
            buttonView.findViewById<LinearLayout>(R.id.button)

        val nameView =
            buttonView.findViewById<TextView>(R.id.textview)

        val imageView =
            buttonView.findViewById<ImageView>(R.id.imageview)

        nameView.text = name

        val drawable = GradientDrawable()
        drawable.cornerRadius = 30f

        val isDark = App.config.data.appThemeInApp == "dark"

        val videoUri: String =
            App.config.data.backgroundImageUri

        val useTransparent =
            videoUri.isNotEmpty()

        if (isDark) {
            if (useTransparent) {
                drawable.setColor("#19111111".toColorInt())
                drawable.setStroke(1, "#19070707".toColorInt())
            } else {
                drawable.setColor("#111111".toColorInt())
                drawable.setStroke(1, "#070707".toColorInt())
            }
        } else {
            if (useTransparent) {
                drawable.setColor("#19FFFFFF".toColorInt())
                drawable.setStroke(1, "#19C6C6C6".toColorInt())
            } else {
                drawable.setColor("#FFFFFF".toColorInt())
                drawable.setStroke(1, "#C6C6C6".toColorInt())
            }
        }

        container.background = drawable

        if (isDark) {
            nameView.setTextColor("#FFFFFF".toColorInt())
        } else {
            nameView.setTextColor("#000000".toColorInt())
        }

        imageView.visibility = View.GONE

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        buttonView.layoutParams = params

        return SmallButtonResult(
            buttonView,
            container,
            nameView,
            imageView
        )
    }
}