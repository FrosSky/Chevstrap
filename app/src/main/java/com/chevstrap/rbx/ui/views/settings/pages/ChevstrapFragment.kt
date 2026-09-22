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

class ChevstrapFragment : Fragment() {
    private val layoutMap = HashMap<Int, LinearLayout>()
    private var manager: ConfigManager? = null
    private var linear1: LinearLayout? = null
    private var linear2: LinearLayout? = null
    private var textviewDesc: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.page_fragment,
            container,
            false
        )

        initialize(view)

        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        try {
            manager = ConfigManager.instance

            initializeLogic()

            val parentLayout = requireNotNull(linear2)

            Chevstrap.addEveryPresets(
                requireContext(),
                parentLayout,
                this
            )

            App.savedFragmentActivity = requireActivity()
        } catch (e: Exception) {
            App.logger.writeException(
                "ChevstrapFragment::onViewCreated",
                e
            )
        }
    }

    override fun onDestroyView() {
        layoutMap.values.forEach {
            it.removeAllViews()
        }

        layoutMap.clear()
        linear1 = null
        linear2 = null
        textviewDesc = null

        super.onDestroyView()
    }

    private fun initialize(view: View) {
        linear1 = view.findViewById(R.id.linear1)
        linear2 = view.findViewById(R.id.linear2)
        textviewDesc = view.findViewById(R.id.textview_desc)
    }

    private fun initializeLogic() {
        textviewDesc?.text = ResourceManagerEx.getStringOrEmpty(
            requireContext(),
            R.string.menu_settings_description
        )

        linear1?.let {
            aStyleButton1(it)
        }
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
        method: MethodPair<*, Boolean>?,
        indexParented: Int
    ) {
        method ?: return

        val parent = if (indexParented > -1) {
            requireNotNull(layoutMap[indexParented])
        } else {
            requireNotNull(linear2)
        }

        val toggle =  ComponentUtils.addToggle(
            context,
            name,
            description,
            method
        )

        parent.addView(toggle.toggleView)
    }

    fun addButton(
        name: String?,
        description: String?,
        method: MethodAction,
        indexParented: Int
    ) {
        val parent = if (indexParented > -1) {
            requireNotNull(layoutMap[indexParented])
        } else {
            requireNotNull(linear2)
        }

        val button =  ComponentUtils.addButton(
            context,
            name,
            description,

            method
        )

        parent.addView(button.buttonView)
    }

    fun addAccordionMenu(
        name: String?,
        description: String?,
        function: Runnable?,
        index: Int
    ) {
        val parent = requireNotNull(linear2)

        val accordion =  ComponentUtils.addAccordionMenu(
            context,
            name,
            description,
            function
        )

        layoutMap[index] = accordion.expandContainer as LinearLayout
        parent.addView(accordion.buttonView)
    }

    fun addTextbox(
        name: String?,
        description: String?,
        method: MethodPair<*, String>?,
        typeIs: String?,
        indexParented: Int
    ) {
        method ?: return

        val parent = if (indexParented > -1) {
            requireNotNull(layoutMap[indexParented])
        } else {
            requireNotNull(linear2)
        }

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
        jsonArray: JSONArray?,
        method: MethodPair<*, String>?,
        indexParented: Int
    ) {
        method ?: return

        val parent = if (indexParented > -1) {
            requireNotNull(layoutMap[indexParented])
        } else {
            requireNotNull(linear2)
        }

        val dropdown =  ComponentUtils.addDropdown(
            context,
            name,
            description,
            jsonArray,
            method
        )

        parent.addView(dropdown.dropDownView)
    }

    private fun aStyleButton1(button: LinearLayout) {
        val drawable = GradientDrawable().apply {
            cornerRadius = 30f
            setColor("#151515".toColorInt())
        }

        button.background = drawable
    }
}