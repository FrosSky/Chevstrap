package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.ui.components.ComponentAppearance
import com.chevstrap.rbx.ui.components.holders.DropdownResult
import com.chevstrap.rbx.ui.viewModels.MethodPair
import org.json.JSONArray
import org.json.JSONException

object DropdownBuilder {

    fun addDropdown(
        context: Context?,
        name: String?,
        description: String?,
        jsonArray: JSONArray?,
        selectedMethod: MethodPair<*, String>?
    ): DropdownResult {

        val labels = ArrayList<String>()
        val inflater = LayoutInflater.from(context)

        val root = LinearLayout(context)

        val dropDownView = inflater.inflate(
            R.layout.custom_dropdown_advanced,
            root,
            false
        )

        val container =
            dropDownView.findViewById<LinearLayout>(R.id.linear_hey)

        val spinner =
            dropDownView.findViewById<Spinner>(R.id.spinnercustom)

        val nameView =
            dropDownView.findViewById<TextView>(R.id.textview_name_option)

        val descView =
            dropDownView.findViewById<TextView>(R.id.textview_is_desc)

        val linearOfDropdown =
            dropDownView.findViewById<LinearLayout>(R.id.linear_of_dropdown)

        val linearInfo =
            dropDownView.findViewById<LinearLayout>(R.id.linear_info)

        val isSmallMode = App.isSmallMode

        val params =
            linearOfDropdown.layoutParams as LinearLayout.LayoutParams

        val spinnerParams =
            spinner.layoutParams as LinearLayout.LayoutParams

        val paramsInfo =
            linearInfo.layoutParams as LinearLayout.LayoutParams

        if (!isSmallMode) {
            container.orientation = LinearLayout.HORIZONTAL

            params.width = 0
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            params.weight = 1f

            paramsInfo.marginEnd = 10

            nameView.layoutParams.width = 450
            descView.layoutParams.width = 450

            spinnerParams.width =
                (180 * dropDownView.resources.displayMetrics.density).toInt()

            spinnerParams.height =
                (35 * dropDownView.resources.displayMetrics.density).toInt()

            nameView.gravity = Gravity.START
            descView.gravity = Gravity.START
        } else {
            container.orientation = LinearLayout.VERTICAL

            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            params.weight = 0f

            paramsInfo.marginEnd = 5

            nameView.layoutParams.width =
                ViewGroup.LayoutParams.MATCH_PARENT

            descView.layoutParams.width =
                ViewGroup.LayoutParams.MATCH_PARENT

            spinnerParams.width =
                LinearLayout.LayoutParams.MATCH_PARENT

            spinnerParams.height =
                (35 * dropDownView.resources.displayMetrics.density).toInt()

            nameView.gravity = Gravity.CENTER
            descView.gravity = Gravity.CENTER
        }

        linearOfDropdown.layoutParams = params
        spinner.layoutParams = spinnerParams

        ComponentAppearance.styleButton(container)
        ComponentAppearance.styleDropdown(spinner)

        nameView.text = name
        descView.text = description

        if (description.isNullOrEmpty()) {
            descView.visibility = View.GONE

            paramsInfo.gravity =
                if (isSmallMode) {
                    Gravity.CENTER
                } else {
                    Gravity.CENTER_VERTICAL
                }
        } else {
            descView.visibility = View.VISIBLE

            paramsInfo.gravity =
                if (isSmallMode) {
                    Gravity.CENTER
                } else {
                    Gravity.TOP or Gravity.START
                }
        }

        linearInfo.layoutParams = paramsInfo

        val popupBg = GradientDrawable()

        val isDark =
            App.config.data.appThemeInApp == "dark"

        val videoUri =
            App.config.data.backgroundImageUri

        val useTransparent =
            videoUri.isNotEmpty()

        if (isDark) {
            nameView.setTextColor(
                "#FFFFFF".toColorInt()
            )

            descView.setTextColor(
                "#ABABAB".toColorInt()
            )

            popupBg.setColor(
                if (useTransparent) {
                    "#66070707".toColorInt()
                } else {
                    "#070707".toColorInt()
                }
            )
        } else {
            nameView.setTextColor(
                "#000000".toColorInt()
            )

            descView.setTextColor(
                "#545454".toColorInt()
            )

            popupBg.setColor(
                if (useTransparent) {
                    "#66FFFFFF".toColorInt()
                } else {
                    "#FFFFFF".toColorInt()
                }
            )
        }

        popupBg.shape = GradientDrawable.RECTANGLE
        popupBg.cornerRadius = 20f

        for (i in 0 until (jsonArray?.length() ?: 0)) {
            try {
                labels.add(
                    jsonArray
                        ?.getJSONObject(i)
                        ?.optString("label", "")
                        ?: ""
                )
            } catch (_: JSONException) {
            }
        }

        spinner.setPopupBackgroundDrawable(popupBg)

        val adapter = object : ArrayAdapter<String>(
            context!!,
            R.layout.custom_dropdown,
            R.id.textview1,
            labels
        ) {

            private fun styleText(view: View): View {
                val textView =
                    view.findViewById<TextView>(R.id.textview1)

                val light =
                    App.config.data.appThemeInApp == "light"

                textView.setTextColor(
                    if (light) {
                        "#000000".toColorInt()
                    } else {
                        "#FFFFFF".toColorInt()
                    }
                )

                return view
            }

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return styleText(
                    super.getView(
                        position,
                        convertView,
                        parent
                    )
                )
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return styleText(
                    super.getDropDownView(
                        position,
                        convertView,
                        parent
                    )
                )
            }
        }

        spinner.adapter = adapter

        val defaultValue = try {
            jsonArray
                ?.getJSONObject(0)
                ?.optString("value", "")
                ?: ""
        } catch (_: JSONException) {
            ""
        }

        var targetSelected = ""

        try {
            targetSelected =
                selectedMethod?.get() ?: ""
        } catch (_: Exception) {
        }

        if (targetSelected.isEmpty()) {
            targetSelected = defaultValue
        }

        var index = -1

        for (i in 0 until (jsonArray?.length() ?: 0)) {
            try {
                val obj =
                    jsonArray?.getJSONObject(i)

                if (
                    obj?.optString("value", "") ==
                    targetSelected
                ) {
                    index =
                        adapter.getPosition(
                            obj.optString("label", "")
                        )
                    break
                }
            } catch (_: JSONException) {
            }
        }

        if (index >= 0) {
            spinner.setSelection(index)
        }

        val lastKnownValue =
            arrayOf(targetSelected)

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    try {
                        val selected =
                            jsonArray?.getJSONObject(position)

                        val value =
                            selected?.optString(
                                "value",
                                ""
                            ) ?: ""

                        val oldValue =
                            lastKnownValue[0]

                        if (value == oldValue) {
                            return
                        }

                        selectedMethod?.set(value)

                        val actualValue =
                            selectedMethod?.get()
                                ?: oldValue

                        if (actualValue != oldValue) {
                            lastKnownValue[0] =
                                actualValue
                        }
                    } catch (_: Exception) {
                    }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        val listener: (String) -> Unit = { value ->

            if (
                value != lastKnownValue[0] &&
                !spinner.isFocused
            ) {
                spinner.post {

                    lastKnownValue[0] = value

                    for (
                    i in 0 until
                            (jsonArray?.length() ?: 0)
                    ) {
                        try {
                            val obj =
                                jsonArray?.getJSONObject(i)

                            if (
                                obj?.optString(
                                    "value",
                                    ""
                                ) == value
                            ) {
                                val idx =
                                    adapter.getPosition(
                                        obj.optString(
                                            "label",
                                            ""
                                        )
                                    )

                                if (idx >= 0) {
                                    spinner.setSelection(idx)
                                }

                                break
                            }
                        } catch (_: JSONException) {
                        }
                    }
                }
            }
        }

        selectedMethod?.addListener(listener)

        dropDownView.addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {

                override fun onViewAttachedToWindow(
                    v: View
                ) = Unit

                override fun onViewDetachedFromWindow(
                    v: View
                ) {
                    selectedMethod?.removeListener(listener)
                }
            }
        )

        val result = DropdownResult(
            dropDownView,
            spinner,
            adapter,
            nameView
        )

        result.isEnabled = true

        ComponentAppearance.updateDropdownAppearance(
            spinner,
            true
        )

        result.disable = Runnable {
            result.isEnabled = false

            ComponentAppearance.updateDropdownAppearance(
                spinner,
                false
            )

            val defaultIdx =
                adapter.getPosition(
                    labels.firstOrNull { label ->
                        try {
                            jsonArray
                                ?.getJSONObject(
                                    labels.indexOf(label)
                                )
                                ?.optString("value", "") ==
                                    defaultValue
                        } catch (_: Exception) {
                            false
                        }
                    } ?: ""
                )

            if (defaultIdx >= 0) {
                spinner.setSelection(defaultIdx)
            }

            if (defaultValue.isNotEmpty()) {
                lastKnownValue[0] =
                    defaultValue

                try {
                    selectedMethod?.set(defaultValue)
                } catch (_: Exception) {
                }
            }
        }

        result.enable = Runnable {
            result.isEnabled = true

            ComponentAppearance.updateDropdownAppearance(
                spinner,
                true
            )
        }

        return result
    }
}