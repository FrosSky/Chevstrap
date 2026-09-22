package com.chevstrap.rbx.ui.views.settings.pages

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R

class LogViewerFragment : Fragment() {

    private lateinit var editText: EditText
    private lateinit var vScrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.log_viewer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editText = view.findViewById(R.id.edittext2)
        vScrollView = view.findViewById(R.id.vscroll1)

        setupLogViewer()
        applyTheme()

        arguments?.getString(ARG_LOG_TEXT)?.let {
            setLogText(it)
        }
    }

    private fun setupLogViewer() {
        editText.apply {
            keyListener = null
            setTextIsSelectable(true)
            isLongClickable = true
            isCursorVisible = false
            showSoftInputOnFocus = false
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private fun applyTheme() {
        val drawable = GradientDrawable()

        if (App.config.data.appThemeInApp == "dark") {
            drawable.setColor("#111111".toColorInt())
            drawable.setStroke(2, "#070707".toColorInt())
            editText.setTextColor(0xFFFFFFFF.toInt())
        } else {
            drawable.setColor("#F0F3F5".toColorInt())
            drawable.setStroke(2, "#EAEDEF".toColorInt())
            editText.setTextColor(0xFF000000.toInt())
        }

        vScrollView.background = drawable
    }

    fun setLogText(text: String) {
        if (!::editText.isInitialized) {
            arguments = (arguments ?: Bundle()).apply {
                putString(ARG_LOG_TEXT, text)
            }
            return
        }

        editText.setText(text)
        editText.setSelection(0)
    }

    companion object {
        private const val ARG_LOG_TEXT = "log_text"

        fun newInstance(logText: String): LogViewerFragment {
            return LogViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOG_TEXT, logText)
                }
            }
        }
    }
}