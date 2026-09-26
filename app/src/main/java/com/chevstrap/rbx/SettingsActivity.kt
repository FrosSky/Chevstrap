package com.chevstrap.rbx

import android.Manifest
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.models.persistable.ConfigData
import com.chevstrap.rbx.ui.Frontend
import com.chevstrap.rbx.ui.views.settings.pages.AboutOneFragment
import com.chevstrap.rbx.ui.views.settings.pages.AccountFragment
import com.chevstrap.rbx.ui.views.settings.pages.BehaviourFragment
import com.chevstrap.rbx.ui.views.settings.pages.ChevstrapFragment
import com.chevstrap.rbx.ui.views.settings.pages.IntegrationsFragment
import com.chevstrap.rbx.ui.views.settings.pages.LogViewerFragment
import com.chevstrap.rbx.ui.views.settings.pages.LoginWebFragment
import com.chevstrap.rbx.ui.views.settings.SettingsScreen
import com.chevstrap.rbx.ui.viewModels.settings.SettingsScreenViewModel
import com.chevstrap.rbx.utility.AppPermissions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewModel: SettingsScreenViewModel

    private lateinit var settingsScreen: SettingsScreen

    lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private val wideScreenWidthDp = 600

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings)

        App.savedSettingsActivity = this

        viewModel = ViewModelProvider(this)[
            SettingsScreenViewModel::class.java
        ]

        settingsScreen = SettingsScreen(
            this,
            viewModel
        )

        try {
            initialize()
            initializeLogic()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                e.message,
                Toast.LENGTH_LONG
            ).show()
        }

        registerImagePicker()
        setupBackButton()
    }

    private fun initialize() {
        Installer().handleUpgrades()
        settingsScreen.initialize()
        setupButtons()
    }

    private fun setupButtons() {

        settingsScreen
            .getContentContainer()

        findViewById<android.widget.LinearLayout>(
            R.id.button_close
        ).setOnClickListener {
            showMessageBoxUnsavedChanges()
        }

        findViewById<android.widget.LinearLayout>(
            R.id.button_save
        ).setOnClickListener {
            App.config.save()
        }

        findViewById<android.widget.LinearLayout>(
            R.id.button_saveandlaunch
        ).setOnClickListener {
            launchClient()
        }
    }

    private fun launchClient() {

        try {

            App.config.save()

            val args = arrayOf("-client")

            val launchSettings =
                LaunchSettings(args)

            val launchHandler =
                LaunchHandler(launchSettings)

            launchHandler.setFragmentManager(
                supportFragmentManager
            )

            launchHandler.run()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                e.message ?: "Failed to launch",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeLogic() {
        val config = resources.configuration

        App.isSmallMode =
            config.screenWidthDp < wideScreenWidthDp

        settingsScreen.updateSmallMode(
            App.isSmallMode
        )

        setupListeners()

        settingsScreen.updateTheme(
            App.config.data.appThemeInApp
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    movePage(
                        SettingsScreenViewModel.PAGE_INTEGRATIONS
                    )
                }
            }
        }
    }

    private fun setupListeners() {
        App.addSmallModeListener(
            smallModeListener
        )

        App.config.listen(ConfigData::appThemeInApp) { value ->
            settingsScreen.updateTheme(value) {
                refreshCurrentPage()
            }
        }

        App.config.listen(ConfigData::backgroundImageUri) { value ->
            settingsScreen.refreshLinear27 {
                if (value.isNotEmpty()) {
                    settingsScreen.updateTheme(App.config.data.appThemeInApp) {
                        refreshCurrentPage()
                    }
                } else {
                    refreshCurrentPage()
                }
            }
        }
    }

    private val smallModeListener: (Boolean) -> Unit =
        { isSmallMode ->

            settingsScreen.updateSmallMode(
                isSmallMode
            )

            settingsScreen.refreshLinear27 {
                refreshCurrentPage()
            }
        }

    override fun onConfigurationChanged(
        newConfig: Configuration
    ) {

        super.onConfigurationChanged(newConfig)

        val isWideScreen =
            newConfig.screenWidthDp >= wideScreenWidthDp

        App.isSmallMode = !isWideScreen

        settingsScreen.updateSmallMode(
            App.isSmallMode
        )

        settingsScreen.refreshLinear27 {
            refreshCurrentPage()
        }
    }

    fun movePage(
        whatPage: String,
        text: String? = null
    ) {

        if (!viewModel.moveToPage(whatPage, text)) {

            if (!viewModel.isValidPage(whatPage)) {

                Frontend.showExceptionDialog(
                    App.savedFragmentActivity,
                    ResourceManagerEx.getStringOrEmpty(
                        App.appContext,
                        R.string.dialog_failed_to_navigate_to_a_page
                    ),
                    IllegalArgumentException(
                        "Unknown page: $whatPage"
                    )
                )
            }

            return
        }

        settingsScreen.setCurrentMenuButton(
            whatPage
        )

        val fragment = try {

            createFragment(
                whatPage,
                text
            )

        } catch (e: Exception) {

            Frontend.showExceptionDialog(
                App.savedFragmentActivity,
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.dialog_failed_to_navigate_to_a_page
                ),
                e
            )

            return
        }

        settingsScreen.setPageTitle(
            viewModel.getPageTitle(whatPage)
        )

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.linear20,
                fragment
            )
            .runOnCommit {

                settingsScreen.refreshCurrentButton()

                val content =
                    settingsScreen.getContentContainer()

                settingsScreen.fadeIn(content)
                settingsScreen.animatePage(content)
            }
            .commit()
    }

    private fun refreshCurrentPage() {

        val page =
            viewModel.currentPage ?: return

        val fragment = try {

            createFragment(
                page,
                viewModel.currentLogText
            )

        } catch (e: Exception) {
            Frontend.showExceptionDialog(
                App.savedFragmentActivity,
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.dialog_failed_to_refresh_page
                ),
                e
            )

            return
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.linear20,
                fragment
            )
            .runOnCommit {

                settingsScreen.refreshCurrentButton()
                settingsScreen.updateAllButtons()

                val content =
                    settingsScreen.getContentContainer()

                settingsScreen.fadeIn(content)
                settingsScreen.animatePage(content)
            }
            .commit()
    }

    private fun createFragment(
        page: String,
        text: String? = null
    ): androidx.fragment.app.Fragment {

        return when (page) {

            SettingsScreenViewModel.PAGE_INTEGRATIONS ->
                IntegrationsFragment()

            SettingsScreenViewModel.PAGE_LAUNCHER ->
                BehaviourFragment()

            SettingsScreenViewModel.PAGE_SETTINGS ->
                ChevstrapFragment()

            SettingsScreenViewModel.PAGE_ABOUT ->
                AboutOneFragment()

            SettingsScreenViewModel.PAGE_LOGGED ->
                AccountFragment()

            SettingsScreenViewModel.PAGE_LOGIN_WEB ->
                LoginWebFragment()

            SettingsScreenViewModel.PAGE_LOG_VIEWER ->
                LogViewerFragment.newInstance(
                    text.orEmpty()
                )

            else ->
                throw IllegalArgumentException(
                    "Unknown page: $page"
                )
        }
    }

    private fun registerImagePicker() {

        imagePickerLauncher =
            registerForActivityResult(
                GetContent()
            ) { uri ->

                if (uri == null) {
                    return@registerForActivityResult
                }

                val path =
                    saveBackgroundImage(uri)
                        ?: return@registerForActivityResult

                App.config.data.backgroundImageUri = path
                App.config.checkForChanges()
            }
    }

    private fun setupBackButton() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    showMessageBoxUnsavedChanges()
                }
            }
        )
    }

    fun saveBackgroundImage(uri: Uri): String? {

        return try {

            val file = File(
                App.appContext?.filesDir,
                "custom_background.png"
            )

            App.appContext
                ?.contentResolver
                ?.openInputStream(uri)
                ?.use { input ->

                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                ?: return null

            if (file.exists()) {
                file.absolutePath
            } else {
                null
            }

        } catch (_: Exception) {
            null
        }
    }

    fun removeBackgroundImage(): Boolean {

        return try {

            val file = File(
                App.appContext?.filesDir,
                "custom_background.png"
            )

            if (file.exists()) {
                file.delete()
            } else {
                true
            }

        } catch (_: Exception) {
            false
        }
    }

    fun requestNotificationPermission() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.TIRAMISU
        ) {
            return
        }

        lifecycleScope.launch {

            AppPermissions.request(
                this@SettingsActivity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    fun showMessageBoxUnsavedChanges() {
        if (App.config.hasUnsavedChanges()) {

            Frontend.showMessageBoxWithRunnable(
                App.savedFragmentActivity,
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.dialog_unsaved_changes
                ),
                true,
                {
                    finish()
                },
                {}
            )

        } else {
            finish()
        }
    }

    override fun onDestroy() {

        if (App.savedSettingsActivity === this) {
            App.savedSettingsActivity = null
        }

        App.config.clearListeners()
        App.removeSmallModeListener(
            smallModeListener
        )

        settingsScreen.clear()

        CustomWatcher
            .getInstance()
            .dispose()

        super.onDestroy()
    }
}
