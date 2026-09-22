package com.chevstrap.rbx.ui.views.settings.pages

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.chevstrap.rbx.App
import com.chevstrap.rbx.ConfigManager
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.components.ComponentUtils
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.MethodPair
import org.json.JSONArray

class BehaviourFragment : Fragment() {
    var manager: ConfigManager? = null
    private var linear1: LinearLayout? = null
    private var textviewDesc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.page_fragment, container, false)
        manager = ConfigManager.instance
        try {
            initialize(view)
            initializeLogic()
        } catch (_: Exception) {
        }
        App.savedFragmentActivity = this.requireActivity()
        return view
    }

    private fun initialize(view: View) {
        linear1 = view.findViewById(R.id.linear1)
        textviewDesc = view.findViewById(R.id.textview_desc)
    }

    private fun initializeLogic() {
        textviewDesc?.text = ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.menu_behaviour_description)
        linear1?.let { aStyleButton1(it) }
    }

    fun addSection(parent: ViewGroup, text: String?) {
        val section =  ComponentUtils.addSection(context, text)
        parent.addView(section.sectionContainer)
    }

    fun addDivider(parent: ViewGroup) {
        val divider =  ComponentUtils.addDivider(context)
        parent.addView(divider.dividerView)
    }

    fun addToggle(
        name: String?,
        description: String?,
        parent: LinearLayout?,
        method: MethodPair<*, Boolean>?
    ) {
        method ?: return

        val toggle =  ComponentUtils.addToggle(
            context,
            name,
            description,
            method
        )

        parent?.addView(toggle.toggleView)
    }

    fun addButton(
        name: String?,
        description: String?,
        parent: LinearLayout,
        method: MethodAction,
    ) {
        val button =  ComponentUtils.addButton(
            context,
            name,
            description,

            method
        )

        parent.addView(button.buttonView)
    }

    fun addTextbox(
        name: String?,
        description: String?,
        parent: LinearLayout,
        method: MethodPair<*, String>?,
        typeIs: String?
    ) {
        method ?: return

        val textbox =  ComponentUtils.addTextbox(
            context,
            name,
            description,
            method,
            typeIs
        )

        parent.addView(textbox.textboxView)
    }

    fun addDropdown(
        name: String?,
        description: String?,
        parent: LinearLayout?,
        jsonArray: JSONArray?,
        method: MethodPair<*, String>?
    ) {
        val dropdown =  ComponentUtils.addDropdown(
            context,
            name,
            description,
            jsonArray,
            method
        )

        parent?.addView(dropdown.dropDownView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parentLayout = view.findViewById<LinearLayout?>(R.id.linear2)
        Behaviour.addEveryPresets(requireContext(), parentLayout, this)
    }

    private fun aStyleButton1(button: LinearLayout) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 30f
        drawable.setColor("#151515".toColorInt())
        button.background = drawable
    }
}
