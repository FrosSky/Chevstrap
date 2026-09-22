package com.chevstrap.rbx.ui.views.customDialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.components.ComponentUtils

class MessageboxFragment : DialogFragment() {
    private var listener: MessageboxListener? = null
    private var messageText = "Default message"
    private var linear1: LinearLayout? = null
    private var textview3: TextView? = null
    private var cancelDisabled = false
    private var okToYes = false
    private var reportExceptionDisabled = false
    private var moreInformation: String? = null

    fun setMessageboxListener(listener: MessageboxListener) {
        this.listener = listener
    }

    fun setMessageText(text: String?) {
        if (text != null) {
            this.messageText = text
        }
    }

    fun disableCancel() {
        this.cancelDisabled = true
    }

    fun replaceOKWithYes() {
        this.okToYes = true
    }

    fun setMoreInformation(text: String?) {
        this.moreInformation = text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!cancelDisabled) {
                        dismiss()
                    }
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.messagebox_fragment, container, false)
        initialize(view)
        initializeLogic()

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        return dialog
    }

    private fun initialize(view: View) {
        var okButtonText = ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.common_ok)

        linear1 = view.findViewById(R.id.linear1)
        val linear2 = view.findViewById<LinearLayout>(R.id.linear2)
        textview3 = view.findViewById(R.id.textview3)

        val linearMoreInformation = view.findViewById<LinearLayout>(R.id.linear_more_information)
        val textviewMoreInformation = view.findViewById<TextView>(R.id.textview_more_information)

        if (okToYes) {
            okButtonText = ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.common_yes)
        }

        val buttonOk = addButton(okButtonText, linear2)
        val buttonCancel = addButton(ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.common_no), linear2)

        if (!okToYes) {
            buttonCancel?.visibility = View.GONE
        }

        if (moreInformation != null) {
            textviewMoreInformation.text = moreInformation
            linearMoreInformation.visibility = View.VISIBLE
        } else {
            linearMoreInformation.visibility = View.GONE
        }

        val drawable = GradientDrawable()
        drawable.cornerRadius = 30f

        if (App.config.data.appThemeInApp == "dark") {
            drawable.setColor("#070707".toColorInt())
            drawable.setStroke(1, "#353535".toColorInt())
        } else if (App.config.data.appThemeInApp == "light") {
            drawable.setColor("#FFFFFF".toColorInt())
            drawable.setStroke(1, "#252525".toColorInt())
        }

        linearMoreInformation.background = drawable
        if (cancelDisabled) {
            buttonCancel?.visibility = View.GONE
        }

        buttonOk?.setOnClickListener { _: View? ->
            if (listener != null) listener!!.onOkClicked()
            dismiss()
        }

        buttonCancel?.setOnClickListener { _: View? ->
            if (listener != null) listener!!.onCancelClicked()
            dismiss()
        }
    }

    private fun initializeLogic() {
        val videoUri: String = App.config.data.backgroundImageUri
        val useTransparent = videoUri.isNotEmpty()

        val drawable = GradientDrawable()
        drawable.cornerRadius = 15f

        if (App.config.data.appThemeInApp == "light") {
            if (useTransparent) {
                drawable.setColor("#99EFEFEF".toColorInt())
                drawable.setStroke(5, "#99EAEAEA".toColorInt())
            } else {
                drawable.setColor("#EFEFEF".toColorInt())
                drawable.setStroke(5, "#EAEAEA".toColorInt())
            }
        } else if (App.config.data.appThemeInApp == "dark") {
            if (useTransparent) {
                drawable.setColor("#99060606".toColorInt())
                drawable.setStroke(5, "#99151515".toColorInt())
            } else {
                drawable.setColor("#060606".toColorInt())
                drawable.setStroke(5, "#151515".toColorInt())
            }
        }

        if (App.config.data.appThemeInApp == "light") {
            textview3!!.setTextColor("#000000".toColorInt())
        } else if (App.config.data.appThemeInApp == "dark") {
            textview3!!.setTextColor("#FFFFFF".toColorInt())
        }

        linear1!!.background = drawable

        if (messageText.isNotEmpty()) {
            textview3!!.text = messageText

            Linkify.addLinks(textview3!!, Linkify.WEB_URLS)
            textview3!!.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    fun addButton(name: String?, parent: LinearLayout): View? {
        if (context == null) {
            return null
        }

        val buttonResult =
             ComponentUtils.addSmallButton(
                context,
                name,
                parent
            )

        buttonResult.buttonOne!!.setPadding(
            buttonResult.buttonOne.paddingLeft + 80,
            buttonResult.buttonOne.paddingTop,
            buttonResult.buttonOne.paddingRight + 80,
            buttonResult.buttonOne.paddingBottom
        )

        if (buttonResult.buttonView!!.parent == null) {
            parent.addView(buttonResult.buttonView)
        }

        return buttonResult.buttonOne
    }

    interface MessageboxListener {
        fun onOkClicked()
        fun onCancelClicked()
    }
}
