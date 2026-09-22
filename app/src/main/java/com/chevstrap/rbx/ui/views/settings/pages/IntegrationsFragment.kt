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
import com.chevstrap.rbx.ui.components.holders.DividerResult
import com.chevstrap.rbx.ui.components.holders.DropdownResult
import com.chevstrap.rbx.ui.components.holders.SectionResult
import com.chevstrap.rbx.ui.components.holders.TextboxResult
import com.chevstrap.rbx.ui.components.holders.ToggleResult
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.MethodPair
import org.json.JSONArray

class IntegrationsFragment : Fragment() {
    var manager: ConfigManager? = null
    private var linear1: LinearLayout? = null
    private val toggleMap: MutableMap<String, ToggleResult> = mutableMapOf()
    private val childMap: MutableMap<String, MutableList<ToggleResult>> = mutableMapOf()
    private val reverseToggleMap = mutableMapOf<ToggleResult, String>()
    private val dropdownMap: MutableMap<String, DropdownResult> = mutableMapOf()
    private val childDropdownMap: MutableMap<String, MutableList<DropdownResult>> = mutableMapOf()
    private var textviewDesc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.page_fragment, container, false)
        manager = ConfigManager.instance
        try {
            initialize(view)
            initializeLogic()
        } catch (_: Exception) {
        }

        App.savedFragmentActivity = this.requireActivity()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        for (toggle in toggleMap.values) {
            toggle.toggleSwitch?.setOnClickListener(null)
        }

        toggleMap.clear()
        childMap.clear()
        reverseToggleMap.clear()

        dropdownMap.clear()
        childDropdownMap.clear()
    }

    private fun initialize(view: View) {
        linear1 = view.findViewById(R.id.linear1)
        textviewDesc = view.findViewById(R.id.textview_desc)
    }

    private fun initializeLogic() {
        textviewDesc?.text = ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.menu_integrations_description)
        linear1?.let { aStyleButton1(it) }
    }

    fun addSection(parent: ViewGroup, text: String?) {
        val section: SectionResult =  ComponentUtils.addSection(context, text)
        parent.addView(section.sectionContainer)
    }

    fun addDivider(parent: ViewGroup) {
        val divider: DividerResult =  ComponentUtils.addDivider(context)
        parent.addView(divider.dividerView)
    }

    fun addButton(
        name: String?,
        description: String?,
        parent: LinearLayout,
        method: MethodAction,
    ) {

        val button = ComponentUtils.addButton(
            context,
            name,
            description,
            method
        )

        parent.addView(button.buttonView)
    }

    private fun propagateChildren(toggleId: String?) {
        if (toggleId == null) return

        val parentToggle = toggleMap[toggleId] ?: return
        val enabled = parentToggle.isChecked

        childMap[toggleId]?.forEach { child ->
            if (enabled) {
                child.enable?.run()
            } else {
                child.disable?.run()
            }

            val childId = reverseToggleMap[child]
            propagateChildren(childId)
        }

        childDropdownMap[toggleId]?.forEach { childDropdown ->
            if (enabled) {
                childDropdown.enable?.run()
            } else {
                childDropdown.disable?.run()
            }
        }
    }

    fun addToggle(
        id: String?,
        name: String?,
        description: String?,
        parent: LinearLayout,
        method: MethodPair<*, Boolean>?,
        controlledParentId: String?
    ) {
        method ?: return

        val toggle =  ComponentUtils.addToggle(
            context,
            name,
            description,
            method
        )

        if (id != null) {
            toggleMap[id] = toggle
            reverseToggleMap[toggle] = id
        }

        if (controlledParentId != null) {
            childMap.getOrPut(controlledParentId) {
                mutableListOf()
            }.add(toggle)
        }

        val originalEnable = toggle.enable
        val originalDisable = toggle.disable

        toggle.enable = Runnable {
            originalEnable?.run()
            propagateChildren(id)
        }

        toggle.disable = Runnable {
            originalDisable?.run()
            propagateChildren(id)
        }

        toggle.onToggleClick = ToggleResult.OnToggleClickListener { _, checked ->
            toggle.isChecked = checked
            propagateChildren(id)
        }

        if (controlledParentId != null) {
            val parentToggle = toggleMap[controlledParentId]

            if (parentToggle?.isChecked == true) {
                originalEnable?.run()
            } else {
                originalDisable?.run()
            }
        }

        parent.addView(toggle.toggleView)
    }

    fun addTextbox(
        name: String?,
        description: String?,
        parent: LinearLayout,
        method: MethodPair<*, String>?,
        typeIs: String?
    ) {
        method ?: return
        val textbox: TextboxResult =  ComponentUtils.addTextbox(
            context,
            name,
            description,
            method,
            typeIs
        )

        parent.addView(textbox.textboxView)
    }

    fun addDropdown(
        id: String?,
        name: String?,
        description: String?,
        parent: LinearLayout,
        jsonArray: JSONArray?,
        method: MethodPair<*, String>?,
        controlledParentId: String?
    ) {
        method ?: return

        val dropdown =  ComponentUtils.addDropdown(
            context,
            name,
            description,
            jsonArray,
            method
        )

        if (id != null) {
            dropdownMap[id] = dropdown
        }

        if (controlledParentId != null) {
            childDropdownMap
                .getOrPut(controlledParentId) {
                    mutableListOf()
                }
                .add(dropdown)
        }

        if (controlledParentId != null) {
            val parentToggle = toggleMap[controlledParentId]

            if (parentToggle?.isChecked == true) {
                dropdown.enable?.run()
            } else {
                dropdown.disable?.run()
            }
        }

        parent.addView(dropdown.dropDownView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val parentLayout: LinearLayout = view.findViewById(R.id.linear2)
        Integrations.addEveryPresets(requireContext(), parentLayout, this)
    }

    private fun aStyleButton1(button: LinearLayout?) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 30f
        drawable.setColor("#151515".toColorInt())
        button?.background = drawable
    }
}