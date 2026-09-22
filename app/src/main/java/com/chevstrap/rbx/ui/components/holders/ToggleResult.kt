package com.chevstrap.rbx.ui.components.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ToggleResult(
    @JvmField val toggleView: View?,
    @JvmField val toggleSwitch: ImageView?,
    @JvmField val nameTextView: TextView?
) {

    @JvmField
    var isChecked: Boolean = false

    @JvmField
    var isEnabled: Boolean = true

    @JvmField
    var enable: Runnable? = null

    @JvmField
    var disable: Runnable? = null

    @JvmField
    var onToggleClick: OnToggleClickListener? = null

    fun interface OnToggleClickListener {
        fun onClick(view: View?, isEnabled: Boolean)
    }
}