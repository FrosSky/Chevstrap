package com.chevstrap.rbx.ui.views.settings.pages

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import chevstrap.gateway.DiscordWsManager
import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.SettingsActivity
import com.chevstrap.rbx.extensions.ResourceManagerEx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class AccountFragment : Fragment() {
    private var viewT: View? = null
    private var textView: TextView? = null
    private var imageV: ImageView? = null
    private var logout: LinearLayout? = null
    private var login: LinearLayout? = null

    private val discordWsManager =
        DiscordWsManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.account_fragment,
            container,
            false
        )
    }

    private fun initialize() {
        val view = viewT ?: return

        login =
            view.findViewById(
                R.id.button_login
            )

        logout =
            view.findViewById(
                R.id.button_logout
            )

        textView =
            view.findViewById(
                R.id.textView4
            )

        imageV =
            view.findViewById(
                R.id.imageView_icon
            )
    }

    private fun initializeLogic() {
        login?.setOnClickListener {
            login()
        }

        logout?.setOnClickListener {
            logout()
        }

        aStyleButtonBRed1(logout)
        aStyleButtonBlack1(login)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        viewT = view
        initialize()
        initializeLogic()

        val token =
            PrefsManager.getToken()

        if (token.isEmpty()) {
            showLoggedOutState()

            val settingsActivity =
                App.savedSettingsActivity

            if (
                settingsActivity is SettingsActivity
            ) {
                settingsActivity.movePage(
                    "LoginWeb"
                )
                App.logger.writeLine(
                    "AccountFragment::onViewCreated",
                    "Moving page to LoginWeb"
                )
            } else {
                App.logger.writeLine(
                    "AccountFragment::onViewCreated",
                    "Settings activity not found"
                )
            }
            return
        }
        showLoggedInLoadingState()

        if (App.userData.isNotEmpty()) {
            updateUser()
        } else {
            checkUser()
        }
    }

    override fun onDestroyView() {
        viewT = null
        textView = null
        imageV = null
        logout = null
        login = null

        super.onDestroyView()
    }

    private fun showLoggedOutState() {
        imageV?.setImageDrawable(null)
        imageV?.visibility = View.GONE

        logout?.visibility = View.GONE
        login?.visibility = View.VISIBLE

        textView?.text = ""
    }

    private fun showLoggedInLoadingState() {
        imageV?.visibility = View.GONE

        logout?.visibility = View.GONE
        login?.visibility = View.GONE

        textView?.text =
            ResourceManagerEx.getStringOrEmpty(
                requireContext(),
                R.string.common_pls_wait
            )
    }

    private fun showLoggedInState() {
        imageV?.visibility = View.VISIBLE
        logout?.visibility = View.VISIBLE
        login?.visibility = View.GONE
    }

    private fun logout() {
        try {
            PrefsManager.setToken("")
            App.userData = ""

            imageV?.setImageDrawable(null)
            imageV?.visibility = View.GONE

            logout?.visibility = View.GONE
            login?.visibility = View.VISIBLE

            textView?.text =
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.common_logout_successful
                )
        } catch (e: Exception) {
            App.logger.writeLine(
                "AccountFragment::logout",
                "Failed to logout"
            )

            App.logger.writeException(
                "AccountFragment::logout",
                e
            )

            textView?.text =
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.common_failed_to_log_out
                )
        }
    }

    private fun login() {
        if (
            !isAdded ||
            viewT == null
        ) {
            return
        }

        showLoggedInLoadingState()
        checkUser()
    }

    private fun checkUser() {
        if (
            !isAdded ||
            viewT == null
        ) {
            return
        }

        if (App.userData.isNotEmpty()) {
            updateUser()
            return
        }

        textView?.text =
            ResourceManagerEx.getStringOrEmpty(
                requireContext(),
                R.string.common_pls_wait
            )

        discordWsManager.setSession(
            PrefsManager.getToken(),
            App.config.data.discordSetOnlineStatus
        )

        discordWsManager.getUserInfo { user ->
            if (!isAdded) {
                return@getUserInfo
            }

            viewLifecycleOwner.lifecycleScope.launch(
                Dispatchers.Main
            ) {
                if (
                    !isAdded ||
                    viewT == null
                ) {
                    return@launch
                }

                if (user == null) {
                    showLoggedOutState()
                    updateFailure()
                    return@launch
                }

                val userId =
                    user.optString(
                        "id"
                    )

                val username =
                    user.optString(
                        "username"
                    )

                if (
                    userId.isBlank() ||
                    username.isBlank()
                ) {
                    showLoggedOutState()
                    updateFailure()
                    return@launch
                }

                val avatar =
                    user.optString(
                        "avatar"
                    )

                val avatarUrl =
                    if (avatar.isNotBlank()) {
                        "https://cdn.discordapp.com/avatars/" +
                                "$userId/$avatar.png"
                    } else {
                        val index =
                            calculateDefaultAvatarIndex(
                                userId
                            )

                        "https://cdn.discordapp.com/embed/avatars/" +
                                "$index.png"
                    }

                val discriminator =
                    user.optString(
                        "discriminator"
                    )

                val disc =
                    if (
                        discriminator.isBlank() ||
                        discriminator == "0"
                    ) {
                        ""
                    } else {
                        discriminator
                    }

                App.userData =
                    "$username#$disc#$avatarUrl"

                updateUser()
            }
        }
    }

    private fun calculateDefaultAvatarIndex(
        userId: String
    ): Int {
        return runCatching {
            (userId.toLong() shr 22)
                .rem(6)
                .toInt()
        }.getOrDefault(0)
    }

    private fun userAvatar(
        imageView: ImageView?
    ) {
        if (imageView == null) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(
            Dispatchers.IO
        ) {
            try {
                val data =
                    App.userData

                val parts =
                    data.split(
                        "#",
                        limit = 3
                    )

                if (parts.size < 3) {
                    return@launch
                }

                val avatarUrl =
                    parts[2]

                if (avatarUrl.isBlank()) {
                    withContext(Dispatchers.Main) {
                        imageView.setImageDrawable(null)
                        imageView.visibility = View.GONE
                    }

                    return@launch
                }

                val input =
                    URL(
                        "$avatarUrl?size=512"
                    ).openStream()

                val image =
                    input.use {
                        BitmapFactory.decodeStream(it)
                    }

                withContext(Dispatchers.Main) {
                    if (
                        image != null &&
                        viewT != null
                    ) {
                        imageView.visibility =
                            View.VISIBLE

                        imageView.setImageBitmap(
                            image
                        )
                    } else {
                        imageView.setImageDrawable(
                            null
                        )

                        imageView.visibility =
                            View.GONE
                    }
                }
            } catch (e: Exception) {
                App.logger.writeLine(
                    "AccountFragment::userAvatar",
                    "Failed to load avatar"
                )

                App.logger.writeException(
                    "AccountFragment::userAvatar",
                    e
                )

                withContext(Dispatchers.Main) {
                    imageView.setImageDrawable(null)
                    imageView.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateUser() {
        if (
            !isAdded ||
            viewT == null
        ) {
            return
        }

        val string =
            App.userData

        if (string.isBlank()) {
            updateFailure()
            return
        }

        val user =
            string.split(
                "#",
                limit = 3
            )

        if (user.size < 3) {
            App.userData = ""
            showLoggedOutState()
            updateFailure()
            return
        }

        val username =
            user[0]

        val discriminator =
            user[1]

        if (username.isBlank()) {
            App.userData = ""
            showLoggedOutState()
            updateFailure()
            return
        }

        textView?.text =
            if (
                discriminator.isEmpty() ||
                discriminator == "0"
            ) {
                username
            } else {
                "$username#$discriminator"
            }

        showLoggedInState()

        userAvatar(imageV)
    }

    private fun aStyleButtonBRed1(
        button: LinearLayout?
    ) {
        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    "#D64545".toColorInt(),
                    "#B72F2F".toColorInt()
                )
            ).apply {
                cornerRadius = 30f
            }

        button?.background = drawable
    }

    private fun aStyleButtonBlack1(
        button: LinearLayout?
    ) {
        val drawable =
            GradientDrawable().apply {
                cornerRadius = 30f
                setColor(
                    "#111111".toColorInt()
                )
            }

        button?.background = drawable
    }

    @SuppressLint("SetTextI18n")
    fun updateFailure() {
        if (
            !isAdded ||
            viewT == null
        ) {
            return
        }

        textView?.text =
            ResourceManagerEx.getStringOrEmpty(
                App.appContext,
                R.string.common_login_failed_try_again
            )
    }
}