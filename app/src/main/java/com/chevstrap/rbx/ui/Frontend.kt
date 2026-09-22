package com.chevstrap.rbx.ui

import androidx.fragment.app.FragmentActivity
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.views.customDialogs.MessageboxFragment

object Frontend {
    fun showExceptionDialog(activity: FragmentActivity?, message: String?, exception: Exception) {
        val dialog = MessageboxFragment()

        dialog.setMessageText(message)
        dialog.disableCancel()
        dialog.setMoreInformation(exception.message)

        dialog.setMessageboxListener(object : MessageboxFragment.MessageboxListener {
            override fun onOkClicked() {
                dialog.dismiss()
            }

            override fun onCancelClicked() {}
        })
        activity?.supportFragmentManager?.let {
            dialog.show(it, "messagebox")
        }
    }

    fun showPlayerErrorDialog(activity: FragmentActivity?, ex: Exception) {
        showMessageBox(
            activity,
            ResourceManagerEx.getStringOrEmpty(
                App.appContext!!,
                R.string.dialog_player_error_help_information
            ) + "https://github.com/" + App.PROJECT_REPOSITORY + "/wiki/Roblox-crashes-or-does-not-launch"
        )
        showExceptionDialog(activity, ex.message, ex)
    }

    fun showMessageBox(activity: FragmentActivity?, message: String?) {
        val dialog = MessageboxFragment()

        dialog.setMessageText(message)
        dialog.disableCancel()

        dialog.setMessageboxListener(object : MessageboxFragment.MessageboxListener {
            override fun onOkClicked() {
                dialog.dismiss()
            }

            override fun onCancelClicked() {}
        })

        activity?.supportFragmentManager?.let {
            dialog.show(it, "messagebox")
        }
    }

    fun showMessageBoxWithRunnable(
        activity: FragmentActivity?, message: String?, useYes: Boolean,
        onOk: Runnable?, onCancel: Runnable?
    ) {
        val dialog = MessageboxFragment()
        dialog.setMessageText(message)

        if (useYes) {
            dialog.replaceOKWithYes()
        }

        //        if (onCancel == null) {
//            dialog.disableCancel();
//        }
        dialog.setMessageboxListener(object : MessageboxFragment.MessageboxListener {
            override fun onOkClicked() {
                dialog.dismiss()
                onOk?.run()
            }

            override fun onCancelClicked() {
                dialog.dismiss()
                onCancel?.run()
            }
        })

        activity?.supportFragmentManager?.let {
            dialog.show(it, "messagebox")
        }
    }
}

