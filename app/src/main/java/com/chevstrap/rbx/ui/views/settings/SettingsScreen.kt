package com.chevstrap.rbx.ui.views.settings

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.doOnLayout
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.SettingsActivity
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.viewModels.settings.SettingsScreenViewModel
import kotlin.math.round

class SettingsScreen(
    private val activity: SettingsActivity,
    private val viewModel: SettingsScreenViewModel
) {

    private val linearInterpolator =
        LinearInterpolator()

    private var themeAnimator: ValueAnimator? = null

    private val dividers =
        mutableListOf<View>()

    private val menuButtonMap =
        HashMap<String, MenuButton>()

    private val menuButtons =
        mutableListOf<MenuButton>()

    private var currentMenuButton: MenuButton? = null

    private val wideScreenWidthDp = 600

    private lateinit var linear20: LinearLayout
    private lateinit var textview5: TextView
    private lateinit var textview1: TextView
    private lateinit var buttonSave: LinearLayout
    private lateinit var buttonSaveAndLaunch: LinearLayout
    private lateinit var buttonClose: LinearLayout
    private lateinit var linearBackground: LinearLayout
    private lateinit var linear27: LinearLayout
    private lateinit var lineardark: LinearLayout
    private lateinit var linear024: LinearLayout

    data class MenuButton(
        val page: String,
        val container: LinearLayout,
        val imageView: ImageView,
        val nameView: TextView,
        val darkIcon: Int,
        val activeIcon: Int,
        val lightIcon: Int,
        val targetTranslationY: () -> Float,
        val activeBackground: GradientDrawable,
        val inactiveBackground: GradientDrawable,
        var currentIcon: Int = -1
    )

    fun initialize() {

        lineardark =
            activity.findViewById(R.id.linear_dark)

        linearBackground =
            activity.findViewById(R.id.linear_background)

        textview1 =
            activity.findViewById(R.id.textview1)

        linear024 =
            activity.findViewById(R.id.linear024)

        linear27 =
            activity.findViewById(R.id.linear27)

        linear20 =
            activity.findViewById(R.id.linear20)

        textview5 =
            activity.findViewById(R.id.textview5)

        buttonSave =
            activity.findViewById(R.id.button_save)

        buttonSaveAndLaunch =
            activity.findViewById(
                R.id.button_saveandlaunch
            )

        buttonClose =
            activity.findViewById(
                R.id.button_close
            )

        createMenu()

        styleBottomButtons()

        val imagePath =
            App.config.data.backgroundImageUri

        if (imagePath.isNotEmpty()) {
            displayCustomBackgroundImage(
                imagePath
            )
        }
    }

    private fun createMenu() {

        addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.menu_integrations_title
            ),
            R.drawable.integrations,
            R.drawable.integrations_on,
            R.drawable.integrations_light,
            SettingsScreenViewModel.PAGE_INTEGRATIONS
        )

        addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.menu_behaviour_title
            ),
            R.drawable.bootstrapper_icon,
            R.drawable.bootstrapper_icon_on,
            R.drawable.bootstrapper_icon_light,
            SettingsScreenViewModel.PAGE_LAUNCHER
        )

        addDivider()

        addButton(
            "Chevstrap",
            R.drawable.setting_icon,
            R.drawable.setting_icon_on,
            R.drawable.setting_icon_light,
            SettingsScreenViewModel.PAGE_SETTINGS
        )

        addButton(
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.about_title
            ),
            R.drawable.about_icon,
            R.drawable.about_icon_on,
            R.drawable.about_icon_light,
            SettingsScreenViewModel.PAGE_ABOUT
        )
    }

    private fun addButton(
        name: String,
        iconResId: Int,
        activeIconResId: Int,
        lightIconResId: Int,
        page: String
    ) {

        val inflater =
            LayoutInflater.from(activity)

        val buttonView =
            inflater.inflate(
                R.layout.button_menu_settingspage,
                linear27,
                false
            )

        val container =
            buttonView.findViewById<LinearLayout>(
                R.id.button_option
            )

        val nameView =
            buttonView.findViewById<TextView>(
                R.id.textview_name
            )

        val imageView =
            buttonView.findViewById<ImageView>(
                R.id.imageview
            )

        nameView.text = name

        updateMenuTextVisibility(
            nameView
        )

        val isDark =
            App.config.data.appThemeInApp ==
                    "dark"

        imageView.setBackgroundResource(
            if (isDark) {
                iconResId
            } else {
                lightIconResId
            }
        )

        nameView.setTextColor(
            if (isDark) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        )

        styleButtonTransparent(
            container
        )

        val density =
            activity.resources.displayMetrics.density

        val activeBackground =
            GradientDrawable().apply {
                cornerRadius =
                    10f * density

                setStroke(
                    0,
                    Color.TRANSPARENT
                )

                setColor(
                    Color.TRANSPARENT
                )
            }

        val inactiveBackground =
            GradientDrawable().apply {
                cornerRadius =
                    5f * density

                setColor(
                    Color.TRANSPARENT
                )
            }

        val menuButton =
            MenuButton(
                page = page,
                container = container,
                imageView = imageView,
                nameView = nameView,
                darkIcon = iconResId,
                activeIcon = activeIconResId,
                lightIcon = lightIconResId,

                targetTranslationY = {

                    val containerCenterY =
                        container.height / 2f

                    val imageCenterY =
                        imageView.top +
                                imageView.height / 2f

                    round(
                        containerCenterY -
                                imageCenterY
                    )
                },

                activeBackground =
                    activeBackground,

                inactiveBackground =
                    inactiveBackground
            )

        menuButtons += menuButton

        menuButtonMap[page] =
            menuButton

        linear27.addView(
            buttonView
        )

        container.setOnClickListener {
            activity.movePage(page)
            updateAllButtons()
        }

        container.doOnLayout {
            updateButton(
                menuButton
            )
        }
    }

    private fun updateMenuTextVisibility(
        textView: TextView
    ) {

        textView.visibility =
            if (App.isSmallMode) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    fun updateButton(
        button: MenuButton
    ) {

        val selected =
            viewModel.currentPage ==
                    button.page

        val isDark =
            App.config.data.appThemeInApp ==
                    "dark"

        val isSmallMode =
            App.isSmallMode

        button.nameView
            .animate()
            .cancel()

        button.imageView
            .animate()
            .cancel()

        button.container
            .animate()
            .cancel()

        button.container.scaleX = 1f
        button.container.scaleY = 1f

        button.imageView.translationY = 0f
        button.nameView.translationY = 0f
        button.nameView.alpha = 1f

        /*
         * SELECTED STATE
         */
        if (selected) {

            button.activeBackground.setColor(
                if (isDark) {
                    "#060606".toColorInt()
                } else {
                    "#FFFFFF".toColorInt()
                }
            )

            button.container.background =
                button.activeBackground

            button.setIcon(
                button.activeIcon
            )

            if (isSmallMode) {

                button.nameView.visibility =
                    View.GONE

                button.container.doOnLayout {

                    if (!App.isSmallMode) {
                        return@doOnLayout
                    }

                    if (
                        button.container.height <= 0 ||
                        button.imageView.height <= 0
                    ) {
                        return@doOnLayout
                    }

                    val containerCenterY =
                        button.container.height / 2f

                    val imageCenterY =
                        button.imageView.top +
                                button.imageView.height / 2f

                    button.imageView.translationY =
                        containerCenterY -
                                imageCenterY
                }

            } else {

                button.nameView.visibility =
                    View.VISIBLE

                button.nameView
                    .animate()
                    .alpha(0f)
                    .translationY(4f)
                    .setInterpolator(
                        linearInterpolator
                    )
                    .setDuration(120L)
                    .start()

                val targetTranslationY =
                    button.targetTranslationY()

                button.imageView
                    .animate()
                    .translationY(
                        targetTranslationY
                    )
                    .setInterpolator(
                        linearInterpolator
                    )
                    .setDuration(120L)
                    .start()

                button.container
                    .animate()
                    .scaleX(1.04f)
                    .scaleY(1.04f)
                    .setInterpolator(
                        linearInterpolator
                    )
                    .setDuration(120L)
                    .start()
            }

        } else {

            button.inactiveBackground.setColor(
                Color.TRANSPARENT
            )

            button.container.background =
                button.inactiveBackground

            button.setIcon(
                if (isDark) {
                    button.darkIcon
                } else {
                    button.lightIcon
                }
            )

            if (isSmallMode) {

                button.nameView.visibility =
                    View.GONE

                button.nameView.alpha =
                    1f

                button.imageView.translationY =
                    0f

            } else {

                button.nameView.visibility =
                    View.VISIBLE

                button.nameView
                    .animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setInterpolator(
                        linearInterpolator
                    )
                    .setDuration(120L)
                    .start()

                button.imageView
                    .animate()
                    .translationY(0f)
                    .setInterpolator(
                        linearInterpolator
                    )
                    .setDuration(120L)
                    .start()

                button.container
                    .animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(
                        linearInterpolator
                    )
                    .setDuration(120L)
                    .start()
            }
        }

        val targetTextColor =
            if (isDark) {
                Color.WHITE
            } else {
                Color.BLACK
            }

        if (
            button.nameView.currentTextColor !=
            targetTextColor
        ) {
            button.nameView.setTextColor(
                targetTextColor
            )
        }
    }

    private fun MenuButton.setIcon(
        iconRes: Int
    ) {

        if (currentIcon == iconRes) {
            return
        }

        imageView.setBackgroundResource(
            iconRes
        )

        currentIcon = iconRes
    }

    fun updateAllButtons() {

        menuButtons.forEach {
            updateButton(it)
        }
    }

    fun setCurrentMenuButton(
        page: String
    ) {

        currentMenuButton =
            menuButtonMap[page]
    }

    fun refreshCurrentButton() {

        currentMenuButton?.let {
            updateButton(it)
        }
    }

    fun refreshLinear27(
        refreshPage: () -> Unit
    ) {

        linear27.doOnLayout {

            menuButtons.forEach {
                updateButton(it)
            }

            refreshPage()
        }
    }

    fun updateSmallMode(
        isSmallMode: Boolean
    ) {

        val params =
            linear27.layoutParams

        params.width =
            if (isSmallMode) {
                200
            } else {
                400
            }

        linear27.layoutParams =
            params

        updateAllButtons()
    }

    fun updateTheme(
        changedVal: String,
        refreshPage: () -> Unit
    ) {

        val isDark =
            changedVal == "dark"

        val imagePath =
            App.config.data.backgroundImageUri

        val useTransparent =
            imagePath.isNotEmpty()

        val bgColor =
            if (isDark) {

                if (useTransparent) {
                    "#4D060606".toColorInt()
                } else {
                    "#060606".toColorInt()
                }

            } else {

                if (useTransparent) {
                    "#4DFFFFFF".toColorInt()
                } else {
                    "#FFFFFF".toColorInt()
                }
            }

        val strokeColor =
            if (isDark) {

                if (useTransparent) {
                    "#19080808".toColorInt()
                } else {
                    "#080808".toColorInt()
                }

            } else {

                if (useTransparent) {
                    "#19E3E8EC".toColorInt()
                } else {
                    "#E3E8EC".toColorInt()
                }
            }

        val textColor =
            if (isDark) {
                "#FFFFFF".toColorInt()
            } else {
                "#000000".toColorInt()
            }

        val dividerColor =
            if (isDark) {
                "#060606".toColorInt()
            } else {
                "#E3E8EC".toColorInt()
            }

        val menuDividerColor =
            if (isDark) {
                "#323232".toColorInt()
            } else {
                "#BCC9D3".toColorInt()
            }

        val oldDrawable =
            lineardark.background
                    as? GradientDrawable

        val startBg =
            oldDrawable?.let {

                if (isDark) {
                    "#FFFFFF".toColorInt()
                } else {
                    "#060606".toColorInt()
                }

            } ?: bgColor

        val startText =
            if (isDark) {
                "#000000".toColorInt()
            } else {
                "#FFFFFF".toColorInt()
            }

        val startMenuDivider =
            if (isDark) {
                "#BCC9D3".toColorInt()
            } else {
                "#323232".toColorInt()
            }

        themeAnimator?.cancel()

        themeAnimator =
            ValueAnimator.ofFloat(
                0f,
                1f
            ).apply {

                duration = 250L

                addUpdateListener { animator ->

                    val fraction =
                        animator.animatedValue
                                as Float

                    val currentBg =
                        ArgbEvaluator().evaluate(
                            fraction,
                            startBg,
                            bgColor
                        ) as Int

                    val currentStroke =
                        ArgbEvaluator().evaluate(
                            fraction,
                            if (isDark) {
                                "#E3E8EC"
                                    .toColorInt()
                            } else {
                                "#080808"
                                    .toColorInt()
                            },
                            strokeColor
                        ) as Int

                    val currentText =
                        ArgbEvaluator().evaluate(
                            fraction,
                            startText,
                            textColor
                        ) as Int

                    val currentDivider =
                        ArgbEvaluator().evaluate(
                            fraction,
                            if (isDark) {
                                "#E3E8EC"
                                    .toColorInt()
                            } else {
                                "#060606"
                                    .toColorInt()
                            },
                            dividerColor
                        ) as Int

                    val currentMenuDivider =
                        ArgbEvaluator().evaluate(
                            fraction,
                            startMenuDivider,
                            menuDividerColor
                        ) as Int

                    val drawable =
                        GradientDrawable().apply {

                            cornerRadius =
                                20f

                            setColor(
                                currentBg
                            )

                            setStroke(
                                2,
                                currentStroke
                            )
                        }

                    lineardark.background =
                        drawable

                    textview5.setTextColor(
                        currentText
                    )

                    textview1.setTextColor(
                        currentText
                    )

                    linear024.setBackgroundColor(
                        currentDivider
                    )

                    dividers.forEach {
                        it.setBackgroundColor(
                            currentMenuDivider
                        )
                    }
                }

                start()
            }

        if (imagePath.isNotEmpty()) {

            try {

                displayCustomBackgroundImage(
                    imagePath
                )

            } catch (_: Exception) {
            }

        } else {

            linearBackground.setBackgroundResource(
                if (isDark) {
                    R.drawable.background_normal
                } else {
                    R.drawable.background_light
                }
            )
        }

        styleButtonBTeal(
            buttonSave
        )

        styleButtonBlack(
            buttonClose
        )

        styleButtonBTeal(
            buttonSaveAndLaunch
        )

        refreshLinear27(
            refreshPage
        )
    }

    fun setPageTitle(
        title: String
    ) {

        textview5.text =
            title
    }

    fun getContentContainer():
            LinearLayout {

        return linear20
    }

    fun animatePage(
        view: View
    ) {

        view.translationY =
            50f

        view.animate()
            .translationY(0f)
            .setDuration(300L)
            .start()
    }

    fun fadeIn(
        view: View?
    ) {

        if (view == null) {
            return
        }

        view.alpha = 0f
        view.visibility =
            View.VISIBLE

        view.animate()
            .alpha(1f)
            .setDuration(300L)
            .start()
    }

    fun displayCustomBackgroundImage(
        path: String
    ) {

        val bitmap =
            BitmapFactory.decodeFile(
                path
            )

        if (bitmap != null) {

            linearBackground.background =
                bitmap.toDrawable(
                    activity.resources
                )
        }
    }

    fun styleBottomButtons() {

        styleButtonBTeal(
            buttonSave
        )

        styleButtonBlack(
            buttonClose
        )

        styleButtonBTeal(
            buttonSaveAndLaunch
        )
    }

    fun styleButtonBTeal(
        button: LinearLayout
    ) {

        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    "#37997D".toColorInt(),
                    "#2B6F5A".toColorInt()
                )
            ).apply {

                cornerRadius =
                    30f
            }

        button.background =
            drawable
    }

    fun styleButtonBlack(
        button: LinearLayout
    ) {

        val drawable =
            GradientDrawable().apply {

                cornerRadius =
                    30f

                setColor(
                    "#111111".toColorInt()
                )
            }

        button.background =
            drawable
    }

    fun styleButtonTransparent(
        button: LinearLayout
    ) {

        val drawable =
            GradientDrawable().apply {

                cornerRadius =
                    5f

                setColor(
                    Color.TRANSPARENT
                )
            }

        button.background =
            drawable
    }

    fun addDivider() {

        val divider =
            View(activity)

        val density =
            activity.resources.displayMetrics.density

        val heightPx =
            (density + 0.5f).toInt()

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightPx
            ).apply {

                setMargins(
                    (10 * density + 0.5f).toInt(),
                    (10 * density + 0.5f).toInt(),
                    (10 * density + 0.5f).toInt(),
                    (10 * density + 0.5f).toInt()
                )
            }

        divider.layoutParams =
            params

        val color =
            if (
                App.config.data.appThemeInApp ==
                "light"
            ) {
                "#BCC9D3".toColorInt()
            } else {
                "#323232".toColorInt()
            }

        divider.setBackgroundColor(
            color
        )

        dividers += divider

        linear27.addView(
            divider
        )
    }

    fun clear() {

        themeAnimator?.cancel()
        themeAnimator = null

        menuButtons.clear()
        menuButtonMap.clear()
        dividers.clear()

        currentMenuButton = null
    }
}