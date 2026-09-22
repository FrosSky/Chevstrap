package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import com.chevstrap.rbx.App
import androidx.core.graphics.toColorInt
import com.chevstrap.rbx.ui.components.holders.DividerResult

object DividerBuilder {
    fun addDivider(context: Context?): DividerResult {
        val divider = View(context)

        val params =
            MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                5
            )

        params.setMargins(10, 25, 10, 20)

        divider.layoutParams = params

        val dark =
            App.config.data.appThemeInApp == "dark"

        divider.setBackgroundColor(
            if (dark)
                "#181818".toColorInt()
            else
                "#E3E8EC".toColorInt()
        )

        return DividerResult(divider)
    }
}