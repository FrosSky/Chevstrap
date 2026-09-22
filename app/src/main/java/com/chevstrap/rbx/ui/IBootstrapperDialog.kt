package com.chevstrap.rbx.ui

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.views.customDialogs.LoadingFragment

class IBootstrapperDialog(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val onCancel: () -> Unit
) {

    var dialog: LoadingFragment? = null
        private set

    fun show() {
        if (dialog?.isAdded == true) return
        val fragment = LoadingFragment()

        fragment.setMessageboxListener(
            object : LoadingFragment.MessageLoadingListener {
                override fun onCancelClicked() {
                    onCancel()
                }
            }
        )

        dialog = fragment
        if (!fragment.isAdded && !fragmentManager.isStateSaved) {
            fragment.show(fragmentManager, "BootstrapperDialog")
        }
    }

    fun updateStatus(resId: Int, progress: String) {
        dialog?.let {
            if (it.isAdded && !it.isStateSaved) {
                it.setMessageText(
                    ResourceManagerEx.getStringOrEmpty(context, resId)
                )
                it.setMessageStatus(progress)
            }
        }
    }

    fun setProgress(progress: String) {
        dialog?.let {
            if (it.isAdded && !it.isStateSaved) {
                it.setMessageStatus(progress)
            }
        }
    }

    fun close() {
        dialog?.let {
            try {
                if (it.isAdded && !it.isStateSaved) {
                    it.dismissAllowingStateLoss()
                }
            } catch (_: Exception) {
            }
        }

        dialog = null
    }

    fun isShowing(): Boolean {
        return dialog?.isAdded == true
    }
}