package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.chevstrap.rbx.App
import com.chevstrap.rbx.ui.components.holders.SectionResult

object SectionBuilder {
    fun addSection(context: Context?, text: String?): SectionResult {
        val container = context?.let { FrameLayout(it) }

        var height = ViewGroup.LayoutParams.WRAP_CONTENT

        if (text == null) {
            height = 0
        }

        container?.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        )

        val sectionText = TextView(context)

        val params =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(10, 25, 10, 5)

        sectionText.layoutParams = params
        sectionText.text = text
        sectionText.textSize = 14f
        sectionText.setTypeface(null, Typeface.BOLD)

        if (App.config.data.appThemeInApp == "light") {
            sectionText.setTextColor("#000000".toColorInt())
        } else {
            sectionText.setTextColor("#FFFFFF".toColorInt())
        }

        container?.addView(sectionText)

        return SectionResult(container, sectionText)
    }
}