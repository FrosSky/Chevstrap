package com.chevstrap.rbx.extensions

import android.content.Context

object ResourceManagerEx {
    fun getStringOrEmpty(
        context: Context?,
        resId: Int
    ): String {
        return context?.getString(resId).orEmpty()
    }
}