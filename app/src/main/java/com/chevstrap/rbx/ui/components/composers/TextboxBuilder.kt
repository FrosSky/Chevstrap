package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.graphics.PorterDuff
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.ui.components.ComponentAppearance
import com.chevstrap.rbx.ui.components.holders.TextboxOnlyResult
import com.chevstrap.rbx.ui.components.holders.TextboxResult
import com.chevstrap.rbx.ui.viewModels.MethodPair

object TextboxBuilder {

    fun addTextbox(
        context: Context?,
        name: String?,
        description: String?,
        selectedMethod: MethodPair<*, String>?,
        typeIs: String?
    ): TextboxResult {

        val inflater = LayoutInflater.from(context)

        val root = LinearLayout(context)
        val textboxView = inflater.inflate(
            R.layout.custom_textbox_advanced,
            root,
            false
        )

        val container =
            textboxView.findViewById<LinearLayout>(R.id.linear_hey)

        val input =
            textboxView.findViewById<EditText>(R.id.edittext)

        val linearTextbox =
            textboxView.findViewById<LinearLayout>(R.id.linear_edittext13)

        val nameView =
            textboxView.findViewById<TextView>(R.id.textview_name_option)

        val descView =
            textboxView.findViewById<TextView>(R.id.textview_is_desc)

        val containerOfIt = textboxView.findViewById<LinearLayout>(R.id.linear_of_textbox)

        val linearInfo =
            textboxView.findViewById<LinearLayout>(R.id.linear_info)

        val isSmallMode = App.isSmallMode
        val params = containerOfIt.layoutParams as LinearLayout.LayoutParams
        val params1 = linearTextbox.layoutParams as LinearLayout.LayoutParams
        val paramsInfo = linearInfo.layoutParams as LinearLayout.LayoutParams

        if (!isSmallMode) {
            container.orientation = LinearLayout.HORIZONTAL
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            params.width = 0
            params1.width = 180

            paramsInfo.marginEnd = 10

            nameView.layoutParams.width = 450
            descView.layoutParams.width = 450

            nameView.gravity = Gravity.START
            descView.gravity = Gravity.START
        } else {
            container.orientation = LinearLayout.VERTICAL
            params.height = 0
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            params1.width = LinearLayout.LayoutParams.MATCH_PARENT

            paramsInfo.marginEnd = 5

            nameView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            descView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

            nameView.gravity = Gravity.CENTER
            descView.gravity = Gravity.CENTER
        }

        containerOfIt.layoutParams = params
        linearTextbox.layoutParams = params1

        ComponentAppearance.styleButton(container)
        ComponentAppearance.styleTextbox(linearTextbox)

        nameView.text = name
        descView.text = description

        if (description.isNullOrEmpty()) {
            descView.visibility = View.GONE

            if (!isSmallMode) {
                paramsInfo.gravity = Gravity.CENTER_VERTICAL
            } else {
                paramsInfo.gravity = Gravity.CENTER
            }
        } else {
            descView.visibility = View.VISIBLE

            if (!isSmallMode) {
                paramsInfo.gravity = Gravity.TOP or Gravity.START
            } else {
                paramsInfo.gravity = Gravity.CENTER
            }
        }
        linearInfo.layoutParams = paramsInfo

        if (App.config.data.appThemeInApp == "light") {
            nameView.setTextColor("#000000".toColorInt())
            descView.setTextColor("#545454".toColorInt())
            input.setTextColor("#000000".toColorInt())
        } else {
            nameView.setTextColor("#FFFFFF".toColorInt())
            descView.setTextColor("#ABABAB".toColorInt())
            input.setTextColor("#FFFFFF".toColorInt())
        }

        input.inputType =
            if (typeIs == "number")
                InputType.TYPE_CLASS_NUMBER
            else
                InputType.TYPE_CLASS_TEXT

        val lastKnownValue = arrayOf("")
        val suppressWatcher = booleanArrayOf(false)

        try {
            val value = selectedMethod?.get()

            if (value != null) {
                lastKnownValue[0] = value
                input.setText(value)
            }
        } catch (_: Exception) {
        }

        input.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {

                if (suppressWatcher[0]) {
                    return
                }

                val value = s.toString()

                lastKnownValue[0] = value

                try {
                    selectedMethod?.set(value)
                } catch (_: Exception) {
                }
            }
        })

        val listener: (String) -> Unit = { value ->

            if (value != lastKnownValue[0] && !input.isFocused) {

                input.post {
                    suppressWatcher[0] = true
                    lastKnownValue[0] = value
                    input.setText(value)
                    suppressWatcher[0] = false
                }
            }
        }

        selectedMethod?.addListener(listener)

        textboxView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View) = Unit

            override fun onViewDetachedFromWindow(v: View) {
                selectedMethod?.removeListener(listener)
            }
        })

        return TextboxResult(
            textboxView,
            input,
            nameView
        )
    }

    fun addTextboxOnly(
        placeholderText: String?,
        parent: LinearLayout?,
        imageResId: Int
    ): TextboxOnlyResult {

        val context = App.appContext

        val inflater = LayoutInflater.from(context)

        val textboxView = inflater.inflate(
            R.layout.custom_the_textbox,
            parent,
            false
        )

        val input =
            textboxView.findViewById<EditText>(R.id.edittext1)

        val linearTextbox =
            textboxView.findViewById<LinearLayout>(R.id.linear1)

        val imageView =
            textboxView.findViewById<ImageView>(R.id.imageview1)

        ComponentAppearance.styleTextbox(linearTextbox)

        if (App.config.data.appThemeInApp == "light") {

            imageView.setColorFilter(
                "#000000".toColorInt(),
                PorterDuff.Mode.SRC_ATOP
            )

        } else {

            imageView.clearColorFilter()
        }

        if (imageResId != 0) {

            val size = (25 * context!!.resources.displayMetrics.density).toInt()

            val params = imageView.layoutParams
            params.width = size
            params.height = size

            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageResource(imageResId)
            imageView.visibility = View.VISIBLE

        } else {

            imageView.visibility = View.GONE
        }

        input.hint = placeholderText

        if (App.config.data.appThemeInApp == "light") {

            input.setTextColor("#000000".toColorInt())

        } else {

            input.setTextColor("#FFFFFF".toColorInt())
        }

        input.inputType = InputType.TYPE_CLASS_TEXT

        return TextboxOnlyResult(
            textboxView,
            linearTextbox,
            input
        )
    }
}