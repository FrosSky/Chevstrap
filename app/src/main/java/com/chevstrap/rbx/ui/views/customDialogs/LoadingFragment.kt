package com.chevstrap.rbx.ui.views.customDialogs

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.components.ComponentUtils
import kotlin.math.min

class LoadingFragment : DialogFragment() {

    interface MessageLoadingListener {
        fun onCancelClicked()
    }

    private var listener: MessageLoadingListener? = null

    private var textviewTaskCurrently: TextView? = null
    private var textviewLoadingStatus: TextView? = null

    private var pendingText: String? = null
    private var pendingStatus: String? = null

    fun setMessageboxListener(listener: MessageLoadingListener?) {
        this.listener = listener
    }

    fun setMessageText(text: String?) {
        pendingText = text
        textviewTaskCurrently?.text = text
    }

    fun setMessageStatus(text: String?) {
        pendingStatus = text
        textviewLoadingStatus?.text = text
    }

    fun addButton(
        name: String?,
        parent: LinearLayout
    ): View? {

        if (context == null) return null

        val buttonResult = ComponentUtils.addSmallButton(
            context,
            name,
            parent
        )

        if (buttonResult.buttonView?.parent == null) {
            parent.addView(buttonResult.buttonView)
        }

        return buttonResult.buttonOne
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.loading_fragment,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(view)
    }

    private fun initialize(view: View) {
        val linear5 = view.findViewById<LinearLayout>(R.id.linear5)
        val linear2 = view.findViewById<LinearLayout>(R.id.linear2)

        textviewTaskCurrently = view.findViewById(R.id.textview_taskcurrently)
        textviewLoadingStatus = view.findViewById(R.id.textview_loadingstatus)

        val buttonCancel = addButton(
            ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.common_cancel),
            linear5
        )

        textviewTaskCurrently?.text = pendingText
        textviewLoadingStatus?.text = pendingStatus

        linear2.background = GradientDrawable().apply {
            cornerRadius = 20f
            setColor("#CC060606".toColorInt())
        }

        buttonCancel?.setOnClickListener {
            listener?.onCancelClicked()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val window = dialog?.window ?: return
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val maxWidth = (400f * density).toInt()
        val screenWidth = displayMetrics.widthPixels
        val finalWidth = min(screenWidth, maxWidth)

        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.width = finalWidth
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        window.setLayout(finalWidth, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        textviewTaskCurrently = null
        textviewLoadingStatus = null
        super.onDestroyView()
    }
}