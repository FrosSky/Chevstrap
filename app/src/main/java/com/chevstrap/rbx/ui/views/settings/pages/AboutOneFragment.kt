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
import com.chevstrap.rbx.R
import com.chevstrap.rbx.App
import com.chevstrap.rbx.ConfigManager
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.components.ComponentUtils
import com.chevstrap.rbx.ui.viewModels.MethodAction

class AboutOneFragment : Fragment() {
    private var textviewDesc: TextView? = null
    private val layoutMap = HashMap<Int?, LinearLayout?>()
    var manager: ConfigManager? = null
    private var linear1: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page_fragment, container, false)
    }

    private fun initialize(view: View) {
        textviewDesc = view.findViewById(R.id.textview_desc)
        linear1 = view.findViewById(R.id.linear1)
    }

    private fun initializeLogic() {
        textviewDesc?.text = ResourceManagerEx.getStringOrEmpty(requireContext(), R.string.about_description)
        linear1?.let { aStyleButton1(it) }
    }

    fun addSection(parent: LinearLayout?, text: String?) {
        val section =  ComponentUtils.addSection(context, text)
        parent?.addView(section.sectionContainer)
    }

    fun addDivider(parent: ViewGroup) {
        val divider =  ComponentUtils.addDivider(context)
        parent.addView(divider.dividerView)
    }

    fun addButton(
        name: String?,
        description: String?,
        parent: LinearLayout?,
        method: MethodAction,
        indexParented: Int
    ) {
        val parent = if (indexParented > -1) {
            requireNotNull(layoutMap[indexParented])
        } else {
            requireNotNull(parent)
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
	    index: Int,
	    parent: LinearLayout?
    ) {
        val accordion =
             ComponentUtils.addAccordionMenu(
                context,
                name,
                description,
                function
            )

        layoutMap[index] = accordion.expandContainer
        parent?.addView(accordion.buttonView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = ConfigManager.instance
        App.savedFragmentActivity = requireActivity()

        initialize(view)
        initializeLogic()

        val parentLayout = view.findViewById<LinearLayout?>(R.id.linear2)
        AboutOne.addEveryPresets(requireContext(), parentLayout, this)
    }

    private fun aStyleButton1(button: LinearLayout) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 30f
        drawable.setColor("#151515".toColorInt())
        button.background = drawable
    }
}