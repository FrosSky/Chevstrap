package com.chevstrap.rbx.ui.components.composers

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.ui.components.ComponentAppearance
import com.chevstrap.rbx.ui.components.holders.AccordionMenuResult

object AccordionBuilder {

    fun addAccordionMenu(
        context: Context?,
        name: String?,
        description: String?,
        function: Runnable?
    ): AccordionMenuResult {
        val inflater = LayoutInflater.from(context)

        val root = LinearLayout(context)
        val buttonView = inflater.inflate(
            R.layout.custom_accordion_menu,
            root,
            false
        )

        val container =
            buttonView.findViewById<LinearLayout>(R.id.linear_hey)

        val container1 =
            buttonView.findViewById<LinearLayout>(R.id.linear_hes)

        val expanderContainer =
            buttonView.findViewById<LinearLayout>(R.id.linear_container_expander)

        val realExpander =
            buttonView.findViewById<LinearLayout>(R.id.linear_expander)

        val nameView =
            buttonView.findViewById<TextView>(R.id.textview_name_option)

        val descView =
            buttonView.findViewById<TextView>(R.id.textview_is_desc)

        val imageViewButton =
            buttonView.findViewById<ImageView>(R.id.imageview_button)

        val linearOfAccordion =
            buttonView.findViewById<LinearLayout>(R.id.linear_of_accordion)

        val isSmallMode = App.isSmallMode

        val params =
            linearOfAccordion.layoutParams as LinearLayout.LayoutParams

        if (!isSmallMode) {
            container1.orientation = LinearLayout.HORIZONTAL
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            params.width = 0
        } else {
            container1.orientation = LinearLayout.VERTICAL
            params.height = 0
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
        }

        linearOfAccordion.layoutParams = params

        ComponentAppearance.styleButton(container)

        nameView.text = name
        descView.text = description

        if (description.isNullOrEmpty()) {
            descView.visibility = View.GONE
        }

        imageViewButton.setBackgroundResource(R.drawable.arrow_down)

        val isDark = App.config.data.appThemeInApp == "dark"

        val videoUri: String =
            App.config.data.backgroundImageUri

        val useTransparent =
            videoUri.isNotEmpty()

        nameView.setTextColor(
            if (isDark) {
                "#FFFFFF".toColorInt()
            } else {
                "#000000".toColorInt()
            }
        )

        descView.setTextColor(
            if (isDark) {
                "#ABABAB".toColorInt()
            } else {
                "#545454".toColorInt()
            }
        )

        imageViewButton.backgroundTintList =
            ColorStateList.valueOf(
                if (isDark) {
                    "#FFFFFF".toColorInt()
                } else {
                    "#000000".toColorInt()
                }
            )

        val drawable = GradientDrawable()

        drawable.cornerRadii = floatArrayOf(
            0f, 0f,
            0f, 0f,
            30f, 30f,
            30f, 30f
        )

        if (isDark) {
            drawable.setColor(
                if (useTransparent) {
                    "#99070707".toColorInt()
                } else {
                    "#000000".toColorInt()
                }
            )
        } else {
            drawable.setColor(
                if (useTransparent) {
                    "#9969757E".toColorInt()
                } else {
                    "#69757E".toColorInt()
                }
            )
        }

        expanderContainer.background = drawable
        expanderContainer.visibility = View.GONE
        expanderContainer.layoutParams.height = 0

        container.setOnClickListener {
            if (expanderContainer.isGone) {
                if (realExpander.isEmpty()) {
                    function?.run()
                }

                expanderContainer.post {
                    val parentWidth =
                        (expanderContainer.parent as? View)?.width
                            ?: container.width

                    if (parentWidth <= 0) {
                        return@post
                    }

                    expanderContainer.measure(
                        View.MeasureSpec.makeMeasureSpec(
                            parentWidth,
                            View.MeasureSpec.EXACTLY
                        ),
                        View.MeasureSpec.makeMeasureSpec(
                            0,
                            View.MeasureSpec.UNSPECIFIED
                        )
                    )

                    val targetHeight =
                        expanderContainer.measuredHeight

                    if (targetHeight <= 0) {
                        return@post
                    }

                    expanderContainer.animate().cancel()

                    expanderContainer.layoutParams.height = 0
                    expanderContainer.visibility = View.VISIBLE
                    expanderContainer.requestLayout()

                    expanderContainer.animate()
                        .setDuration(150)
                        .setUpdateListener { animation ->
                            val fraction =
                                animation.animatedFraction

                            expanderContainer.layoutParams.height =
                                (targetHeight * fraction).toInt()

                            expanderContainer.requestLayout()
                        }
                        .withEndAction {
                            expanderContainer.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT

                            expanderContainer.requestLayout()
                        }
                        .start()
                }
            } else {
                val initialHeight =
                    expanderContainer.height

                if (initialHeight <= 0) {
                    expanderContainer.layoutParams.height = 0
                    expanderContainer.visibility = View.GONE
                    expanderContainer.requestLayout()

                    return@setOnClickListener
                }

                expanderContainer.animate().cancel()

                expanderContainer.animate()
                    .setDuration(150)
                    .setUpdateListener { animation ->
                        val fraction =
                            animation.animatedFraction

                        expanderContainer.layoutParams.height =
                            (initialHeight * (1f - fraction)).toInt()

                        expanderContainer.requestLayout()
                    }
                    .withEndAction {
                        expanderContainer.layoutParams.height = 0
                        expanderContainer.visibility = View.GONE
                        expanderContainer.requestLayout()
                    }
                    .start()
            }
        }

        return AccordionMenuResult(
            buttonView,
            container,
            nameView,
            realExpander,
            expanderContainer
        )
    }
}