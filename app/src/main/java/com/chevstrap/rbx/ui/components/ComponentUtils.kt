package com.chevstrap.rbx.ui.components

import android.content.Context
import android.widget.LinearLayout
import com.chevstrap.rbx.ui.components.composers.ButtonBuilder
import com.chevstrap.rbx.ui.components.composers.AccordionBuilder
import com.chevstrap.rbx.ui.components.composers.DividerBuilder
import com.chevstrap.rbx.ui.components.composers.DropdownBuilder
import com.chevstrap.rbx.ui.components.composers.SectionBuilder
import com.chevstrap.rbx.ui.components.composers.SmallButtonBuilder
import com.chevstrap.rbx.ui.components.composers.TextboxBuilder
import com.chevstrap.rbx.ui.components.composers.ToggleBuilder
import com.chevstrap.rbx.ui.components.holders.AccordionMenuResult
import com.chevstrap.rbx.ui.components.holders.ButtonResult
import com.chevstrap.rbx.ui.components.holders.DividerResult
import com.chevstrap.rbx.ui.components.holders.DropdownResult
import com.chevstrap.rbx.ui.components.holders.SectionResult
import com.chevstrap.rbx.ui.components.holders.SmallButtonResult
import com.chevstrap.rbx.ui.components.holders.TextboxOnlyResult
import com.chevstrap.rbx.ui.components.holders.TextboxResult
import com.chevstrap.rbx.ui.components.holders.ToggleResult
import com.chevstrap.rbx.ui.viewModels.MethodAction
import com.chevstrap.rbx.ui.viewModels.MethodPair
import org.json.JSONArray

object ComponentUtils {

    @JvmStatic
    fun addSection(
        context: Context?,
        text: String?
    ): SectionResult {
        return SectionBuilder.addSection(context, text)
    }

    @JvmStatic
    fun addDivider(
        context: Context?
    ): DividerResult {
        return DividerBuilder.addDivider(context)
    }

    @JvmStatic
    fun addTextbox(
        context: Context?,
        name: String?,
        description: String?,
        selectedMethod: MethodPair<*, String>?,
        typeIs: String?
    ): TextboxResult {
        return TextboxBuilder.addTextbox(
            context,
            name,
            description,
            selectedMethod,
            typeIs
        )
    }

    @JvmStatic
    fun addTextboxOnly(
        placeholder: String?,
        parent: LinearLayout?,
        imageResId: Int
    ): TextboxOnlyResult {
        return TextboxBuilder.addTextboxOnly(
            placeholder,
            parent,
            imageResId
        )
    }

    @JvmStatic
    fun addDropdown(
        context: Context?,
        name: String?,
        description: String?,
        jsonArray: JSONArray?,
        selectedMethod: MethodPair<*, String>?
    ): DropdownResult {
        return DropdownBuilder.addDropdown(
            context,
            name,
            description,
            jsonArray,
            selectedMethod
        )
    }

    @JvmStatic
    fun addToggle(
        context: Context?,
        name: String?,
        description: String?,
        selectedMethod: MethodPair<*, Boolean>
    ): ToggleResult {
        return ToggleBuilder.addToggle(
            context,
            name,
            description,
            selectedMethod
        )
    }

    @JvmStatic
    fun addButton(
        context: Context?,
        name: String?,
        description: String?,
        selectedMethod: MethodAction?
    ): ButtonResult {
        return ButtonBuilder.addButton(
            context,
            name,
            description,
            selectedMethod,
        )
    }

    @JvmStatic
    fun addSmallButton(
        context: Context?,
        name: String?,
        parent: LinearLayout?
    ): SmallButtonResult {
        return SmallButtonBuilder.addSmallButton(
            context,
            name,
            parent
        )
    }

    @JvmStatic
    fun addAccordionMenu(
        context: Context?,
        name: String?,
        description: String?,
        function: Runnable?
    ): AccordionMenuResult {
        return AccordionBuilder.addAccordionMenu(
            context,
            name,
            description,
            function
        )
    }
}