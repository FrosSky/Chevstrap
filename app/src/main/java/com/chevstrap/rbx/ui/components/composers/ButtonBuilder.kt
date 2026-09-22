package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.ui.components.ComponentAppearance
import com.chevstrap.rbx.ui.components.holders.ButtonResult
import com.chevstrap.rbx.ui.viewModels.MethodAction

object ButtonBuilder {

    fun addButton(
        context: Context?,
        name: String?,
        description: String?,
        selectedMethod: MethodAction?
    ): ButtonResult {
        val inflater = LayoutInflater.from(context)

        val root = LinearLayout(context)
        val buttonView = inflater.inflate(
            R.layout.custom_button_advanced,
            root,
            false
        )

        val container =
            buttonView.findViewById<LinearLayout>(R.id.linear_hey)

        val nameView =
            buttonView.findViewById<TextView>(R.id.textview_name_option)

        val descView =
            buttonView.findViewById<TextView>(R.id.textview_is_desc)

        val linearOfButton =
            buttonView.findViewById<LinearLayout>(R.id.linear_of_button)

        val imageViewButton =
            buttonView.findViewById<ImageView>(R.id.imageview_button)

        val linearInfo =
            buttonView.findViewById<LinearLayout>(R.id.linear_info)

        val isSmallMode = App.isSmallMode

        val params =
            linearOfButton.layoutParams as LinearLayout.LayoutParams

        val paramsInfo =
            linearInfo.layoutParams as LinearLayout.LayoutParams

        if (!isSmallMode) {
            container.orientation = LinearLayout.HORIZONTAL

            params.height =
                LinearLayout.LayoutParams.WRAP_CONTENT

            params.width = 0

            paramsInfo.marginEnd = 10

            nameView.gravity = Gravity.START
            descView.gravity = Gravity.START

            nameView.width = 450
            descView.width = 450
        } else {
            container.orientation = LinearLayout.VERTICAL

            params.height = 0
            params.width =
                LinearLayout.LayoutParams.MATCH_PARENT

            paramsInfo.marginEnd = 5

            nameView.gravity = Gravity.CENTER
            descView.gravity = Gravity.CENTER

            nameView.layoutParams.width =
                ViewGroup.LayoutParams.MATCH_PARENT

            descView.layoutParams.width =
                ViewGroup.LayoutParams.MATCH_PARENT
        }

        linearOfButton.layoutParams = params

        ComponentAppearance.styleButton(container)

        nameView.text = name
        descView.text = description

        if (description.isNullOrEmpty()) {
            descView.visibility = View.GONE

            paramsInfo.gravity = if (!isSmallMode) {
                Gravity.CENTER_VERTICAL
            } else {
                Gravity.CENTER
            }
        } else {
            descView.visibility = View.VISIBLE

            paramsInfo.gravity = if (!isSmallMode) {
                Gravity.TOP or Gravity.START
            } else {
                Gravity.CENTER
            }
        }

        linearInfo.layoutParams = paramsInfo

        val isDark =
            App.config.data.appThemeInApp == "dark"

        if (!isDark) {
            nameView.setTextColor("#000000".toColorInt())
            descView.setTextColor("#545454".toColorInt())
        } else {
            nameView.setTextColor("#FFFFFF".toColorInt())
            descView.setTextColor("#ABABAB".toColorInt())
        }

        imageViewButton.backgroundTintList =
            ColorStateList.valueOf(
                if (isDark) {
                    "#FFFFFF".toColorInt()
                } else {
                    "#000000".toColorInt()
                }
            )

        container.setOnClickListener {
            selectedMethod?.invoke()
        }

        return ButtonResult(
            buttonView,
            container,
            nameView
        )
    }
}