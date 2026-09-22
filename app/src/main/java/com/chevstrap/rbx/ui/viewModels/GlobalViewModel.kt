package com.chevstrap.rbx.ui.viewModels

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object GlobalViewModel {
    @JvmStatic
    fun openWebpage(context: Context?, getLink: String?) {
        if (getLink.isNullOrEmpty()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, getLink.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}