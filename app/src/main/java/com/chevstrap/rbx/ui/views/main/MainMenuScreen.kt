package com.chevstrap.rbx.ui.views.main

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.components.ComponentUtils
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.main.MainMenuScreenViewModel

class MainMenuScreen(
    private val activity: Activity,
    private val viewModel: MainMenuScreenViewModel
) {

    companion object {
        private const val WIDE_SCREEN_WIDTH_DP = 600
    }

    private lateinit var linearBackground: LinearLayout
    private lateinit var linear2: LinearLayout
    private lateinit var linear10: LinearLayout
    private lateinit var linear5: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var textViewVersion: TextView

    fun initialize() {
        linearBackground =
            activity.findViewById(R.id.linear_background)

        linear2 =
            activity.findViewById(R.id.linear2)

        linear10 =
            activity.findViewById(R.id.linear10)

        linear5 =
            activity.findViewById(R.id.linear5)

        imageView =
            activity.findViewById(R.id.imageview1)

        textViewVersion =
            activity.findViewById(R.id.textview_version)

        setupButtons()
        setupTheme()
        setupVersion()
    }

    private fun setupButtons() {
        val buttonLauncher =
             ComponentUtils.addButton(
                activity.applicationContext,
                ResourceManagerEx.getStringOrEmpty(
                    activity,
                    R.string.launch_menu_launch_roblox
                ),
                "",
                //linear5,
                 MethodAction {
                     viewModel.launchRoblox()
                 }
            )

        val buttonConfigureSettings =
             ComponentUtils.addButton(
                activity.applicationContext,
                ResourceManagerEx.getStringOrEmpty(
                    activity,
                    R.string.launch_menu_configure_settings
                ),
                "",
                //linear5,
                MethodAction {
                    viewModel.menuSettings()
                }
            )

        val buttonWiki =
             ComponentUtils.addButton(
                activity.applicationContext,
                ResourceManagerEx.getStringOrEmpty(
                    activity,
                    R.string.launch_menu_wiki
                ),
                "",
                //linear5,
                MethodAction {
                    viewModel.projectWikiPage()
                }
            )

        linear5.addView(buttonLauncher.buttonView)
        linear5.addView(buttonConfigureSettings.buttonView)
        linear5.addView(buttonWiki.buttonView)
    }

    private fun setupTheme() {
        val isDark =
            App.config.data.appThemeInApp == "dark"

        linearBackground.setBackgroundResource(
            if (isDark) {
                R.drawable.background_normal
            } else {
                R.drawable.background_light
            }
        )

        imageView.setImageResource(
            if (isDark) {
                R.drawable.logo_and_icon
            } else {
                R.drawable.logo_and_icon_light
            }
        )
    }

    private fun setupVersion() {
        try {
            textViewVersion.text =
                activity.packageManager
                    .getPackageInfo(
                        activity.packageName,
                        0
                    )
                    .versionName
        } catch (_: Exception) {
        }
    }

    fun updateLayoutOrientation(
        isSmallMode: Boolean
    ) {
        val linear10Params =
            linear10.layoutParams as LinearLayout.LayoutParams

        val linear5Params =
            linear5.layoutParams as LinearLayout.LayoutParams

        val imageParams =
            imageView.layoutParams as ViewGroup.MarginLayoutParams

        if (isSmallMode) {
            linear2.orientation =
                LinearLayout.VERTICAL

            linear2.gravity =
                Gravity.CENTER

            linear10.orientation =
                LinearLayout.VERTICAL

            linear10.gravity =
                Gravity.CENTER

            linear10Params.width =
                LinearLayout.LayoutParams.MATCH_PARENT

            linear10Params.height =
                LinearLayout.LayoutParams.WRAP_CONTENT

            linear10Params.weight = 0f

            linear5.gravity =
                Gravity.CENTER

            linear5Params.width =
                LinearLayout.LayoutParams.WRAP_CONTENT

            linear5Params.height =
                LinearLayout.LayoutParams.WRAP_CONTENT

            linear5Params.weight = 0f

            imageParams.marginEnd = 0

        } else {
            linear2.orientation =
                LinearLayout.HORIZONTAL

            linear2.gravity =
                Gravity.CENTER_VERTICAL

            linear10.orientation =
                LinearLayout.VERTICAL

            linear10.gravity =
                Gravity.CENTER_VERTICAL

            linear10Params.width =
                LinearLayout.LayoutParams.WRAP_CONTENT

            linear10Params.height =
                LinearLayout.LayoutParams.WRAP_CONTENT

            linear10Params.weight = 0f

            linear5.gravity =
                Gravity.NO_GRAVITY

            linear5Params.width = 0

            linear5Params.height =
                LinearLayout.LayoutParams.WRAP_CONTENT

            linear5Params.weight = 1f

            imageParams.marginEnd =
                (30 * activity.resources.displayMetrics.density)
                    .toInt()
        }

        linear10.layoutParams =
            linear10Params

        linear5.layoutParams =
            linear5Params

        imageView.layoutParams =
            imageParams

        linearBackground.requestLayout()
    }

    fun isSmallMode(): Boolean {
        return activity.resources.configuration.screenWidthDp <
                WIDE_SCREEN_WIDTH_DP
    }
}