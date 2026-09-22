package com.chevstrap.rbx.ui.components.composers

import android.content.Context
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
import com.chevstrap.rbx.ui.components.holders.ToggleResult
import com.chevstrap.rbx.ui.viewModels.MethodPair

object ToggleBuilder {

    fun addToggle(
        context: Context?,
        name: String?,
        description: String?,
        selectedMethod: MethodPair<*, Boolean>
    ): ToggleResult {

        val inflater = LayoutInflater.from(context)

        val root = LinearLayout(context)
        val toggleView = inflater.inflate(
            R.layout.custom_toggle_advanced,
            root,
            false
        )

        val container =
            toggleView.findViewById<LinearLayout>(R.id.linear_hey)

        val toggleSwitch =
            toggleView.findViewById<ImageView>(R.id.imageview_switch)

        val nameView =
            toggleView.findViewById<TextView>(R.id.textview_name_option)

        val descView =
            toggleView.findViewById<TextView>(R.id.textview_is_desc)

        val linearOfToggle =
            toggleView.findViewById<LinearLayout>(R.id.linear_of_toggle)

        val linearInfo =
            toggleView.findViewById<LinearLayout>(R.id.linear_info)

        if (App.config.data.appThemeInApp == "light") {
            nameView.setTextColor("#000000".toColorInt())
            descView.setTextColor("#545454".toColorInt())
        } else {
            nameView.setTextColor("#FFFFFF".toColorInt())
            descView.setTextColor("#ABABAB".toColorInt())
        }

        val isSmallMode = App.isSmallMode

        val params =
            linearOfToggle.layoutParams as LinearLayout.LayoutParams

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

            nameView.layoutParams.width = 450
            descView.layoutParams.width = 450
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

        linearOfToggle.layoutParams = params

        ComponentAppearance.styleButton(container)

        nameView.text = name
        descView.text = description

        if (description.isNullOrEmpty()) {
            descView.visibility = View.GONE

            if (!isSmallMode) {
                paramsInfo.gravity =
                    Gravity.CENTER_VERTICAL
            } else {
                paramsInfo.gravity =
                    Gravity.CENTER
            }
        } else {
            descView.visibility = View.VISIBLE

            if (!isSmallMode) {
                paramsInfo.gravity =
                    Gravity.TOP or Gravity.START
            } else {
                paramsInfo.gravity =
                    Gravity.CENTER
            }
        }

        linearInfo.layoutParams = paramsInfo

        val result = ToggleResult(
            toggleView,
            toggleSwitch,
            nameView
        )

        /*
         * Initial state
         */
        try {
            result.isChecked = selectedMethod.get()
        } catch (_: Exception) {
            result.isChecked = false
        }

        result.isEnabled = true

        ComponentAppearance.updateToggleAppearance(
            toggleSwitch,
            result.isChecked,
            true
        )

        /*
         * Used to prevent the listener from updating
         * the appearance while a click is processing.
         */
        var isUpdatingFromClick = false

        /*
         * Toggle click
         *
         * get() -> set() -> get() -> compare
         */
        toggleSwitch.setOnClickListener { view ->

            if (!result.isEnabled) {
                return@setOnClickListener
            }

            try {
                /*
                 * Always use selectedMethod.get()
                 * as the old value.
                 */
                val oldValue = selectedMethod.get()

                /*
                 * Toggle based on the actual value,
                 * not result.isChecked.
                 */
                val requestedValue = !oldValue

                /*
                 * Prevent the external listener from
                 * updating the appearance while set()
                 * is being processed.
                 */
                isUpdatingFromClick = true

                selectedMethod.set(requestedValue)

                /*
                 * Get the actual value after set().
                 */
                val actualValue = selectedMethod.get()

                isUpdatingFromClick = false

                /*
                 * Only update the UI if the value
                 * actually changed.
                 */
                if (actualValue != oldValue) {

                    result.isChecked = actualValue

                    ComponentAppearance.updateToggleAppearance(
                        toggleSwitch,
                        actualValue,
                        true
                    )

                    result.onToggleClick?.onClick(
                        view,
                        actualValue
                    )
                }

            } catch (_: Exception) {
                isUpdatingFromClick = false
            }
        }

        /*
         * Disable
         */
        result.disable = Runnable {

            try {
                val oldValue = selectedMethod.get()

                result.isEnabled = false
                toggleSwitch.isClickable = false

                selectedMethod.set(false)

                val actualValue = selectedMethod.get()

                if (actualValue != oldValue) {
                    result.isChecked = actualValue

                    ComponentAppearance.updateToggleAppearance(
                        toggleSwitch,
                        actualValue,
                        false
                    )
                } else {
                    ComponentAppearance.updateToggleAppearance(
                        toggleSwitch,
                        result.isChecked,
                        false
                    )
                }

            } catch (_: Exception) {

                result.isEnabled = false
                result.isChecked = false

                toggleSwitch.isClickable = false

                ComponentAppearance.updateToggleAppearance(
                    toggleSwitch,
                    false,
                    false
                )
            }
        }

        /*
         * Enable
         */
        result.enable = Runnable {

            result.isEnabled = true
            toggleSwitch.isClickable = true

            try {
                val actualValue =
                    selectedMethod.get()

                if (actualValue != result.isChecked) {
                    result.isChecked = actualValue
                }

                ComponentAppearance.updateToggleAppearance(
                    toggleSwitch,
                    actualValue,
                    true
                )

            } catch (_: Exception) {

                ComponentAppearance.updateToggleAppearance(
                    toggleSwitch,
                    result.isChecked,
                    true
                )
            }
        }

        /*
         * External value listener
         */
        val listener: (Boolean) -> Unit = listener@{ value ->

            /*
             * Ignore the callback caused by our own
             * selectedMethod.set() during a click.
             */
            if (isUpdatingFromClick) {
                return@listener
            }

            if (value != result.isChecked) {

                toggleSwitch.post {

                    /*
                     * Check again because post() is asynchronous.
                     */
                    if (isUpdatingFromClick) {
                        return@post
                    }

                    if (value != result.isChecked) {

                        result.isChecked = value

                        ComponentAppearance.updateToggleAppearance(
                            toggleSwitch,
                            result.isChecked,
                            result.isEnabled
                        )
                    }
                }
            }
        }

        selectedMethod.addListener(listener)

        /*
         * Remove listener when detached.
         */
        toggleView.addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {

                override fun onViewAttachedToWindow(
                    v: View
                ) = Unit

                override fun onViewDetachedFromWindow(
                    v: View
                ) {
                    selectedMethod.removeListener(listener)
                }
            }
        )

        return result
    }
}