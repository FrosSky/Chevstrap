package com.chevstrap.rbx.ui.components.holders

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

class DropdownResult(
    @JvmField val dropDownView: View?,
    @JvmField val spinner: Spinner?,
    @JvmField val adapter: ArrayAdapter<*>?,
    @JvmField val nameTextView: TextView?
) {

    @JvmField
    var isEnabled: Boolean = true

    @JvmField
    var enable: Runnable? = null

    @JvmField
    var disable: Runnable? = null
}