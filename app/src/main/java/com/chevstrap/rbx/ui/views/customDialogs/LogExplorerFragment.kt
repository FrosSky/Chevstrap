package com.chevstrap.rbx.ui.views.customDialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import chevstrap.extensions.FileTool
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.SettingsActivity
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.ui.components.ComponentUtils
import com.chevstrap.rbx.ui.components.holders.SmallButtonResult
import com.chevstrap.rbx.ui.viewModels.customDialogs.LogExplorerViewModel
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class LogExplorerFragment : DialogFragment() {

    private var linear1: LinearLayout? = null

    private lateinit var viewModel: LogExplorerViewModel

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[
            LogExplorerViewModel::class.java
        ]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.log_explorer_fragment,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        initialize(view)
        initializeLogic()

        viewModel.logFiles.observe(
            viewLifecycleOwner
        ) { files ->
            updateAdapter(
                view,
                files
            )
        }

        viewModel.loadData()
    }

    override fun onDestroyView() {
        linear1 = null
        super.onDestroyView()
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        return super.onCreateDialog(
            savedInstanceState
        ).apply {
            window?.setBackgroundDrawable(
                Color.TRANSPARENT.toDrawable()
            )
        }
    }

    private fun initialize(view: View) {
        val context = requireContext()

        val listView = view.findViewById<ListView>(
            R.id.listview
        )

        linear1 = view.findViewById(
            R.id.linear1
        )

        val title = view.findViewById<TextView>(
            R.id.textview_is_desc
        )

        val bottomLayout = view.findViewById<LinearLayout>(
            R.id.linear_bottom_list
        )

        title.text = ResourceManagerEx.getStringOrEmpty(
            context,
            R.string.context_menu_logs_viewer_title
        )

        val buttonClose = addButton(
            ResourceManagerEx.getStringOrEmpty(
                context,
                R.string.common_close
            ),
            bottomLayout
        )

        buttonClose?.setOnClickListener {
            dismiss()
        }

        val light =
            App.config.data.appThemeInApp == "light"

        title.setTextColor(
            if (light) {
                Color.BLACK
            } else {
                Color.WHITE
            }
        )

        val listBackground = GradientDrawable().apply {
            cornerRadius = 20f
            setColor(
                "#070707".toColorInt()
            )
        }

        listView.background = listBackground
    }

    private fun initializeLogic() {
        val linear = linear1 ?: return

        val backgroundUri =
            App.config.data.backgroundImageUri

        val useTransparent =
            backgroundUri.isNotEmpty()

        val light =
            App.config.data.appThemeInApp == "light"

        val background = GradientDrawable().apply {
            cornerRadius = 15f

            if (light) {
                if (useTransparent) {
                    setColor(
                        "#99EFEFEF".toColorInt()
                    )

                    setStroke(
                        5,
                        "#99EAEAEA".toColorInt()
                    )
                } else {
                    setColor(
                        "#EFEFEF".toColorInt()
                    )

                    setStroke(
                        5,
                        "#EAEAEA".toColorInt()
                    )
                }
            } else {
                if (useTransparent) {
                    setColor(
                        "#99060606".toColorInt()
                    )

                    setStroke(
                        5,
                        "#99151515".toColorInt()
                    )
                } else {
                    setColor(
                        "#060606".toColorInt()
                    )

                    setStroke(
                        5,
                        "#151515".toColorInt()
                    )
                }
            }
        }

        linear.background = background
    }

    private fun updateAdapter(
        rootView: View,
        files: List<File>
    ) {
        val listView = rootView.findViewById<ListView>(
            R.id.listview
        )

        listView.adapter = LogAdapter(
            files,
            viewModel.logDirectory
        )
    }

    class LogAdapter(
        private val files: List<File>,
        private val allowedLogDir: File
    ) : BaseAdapter() {

        override fun getCount(): Int {
            return files.size
        }

        override fun getItem(position: Int): File {
            return files[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            val view: View
            val holder: ViewHolder

            if (convertView == null) {
                view = LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.file_explorer_item_history,
                    parent,
                    false
                )

                holder = ViewHolder(
                    linearHey = view.findViewById(
                        R.id.linear_hey
                    ),
                    linearView = view.findViewById(
                        R.id.linear_view
                    ),
                    imageViewThumbnail = view.findViewById(
                        R.id.imageview_thumbnail
                    ),
                    nameText = view.findViewById(
                        R.id.textview_name_option
                    ),
                    descText = view.findViewById(
                        R.id.textview_description_option
                    )
                )

                view.tag = holder
            } else {
                view = convertView

                holder = convertView.tag as ViewHolder
            }

            val dark =
                App.config.data.appThemeInApp == "dark"

            val useTransparent =
                App.config.data.backgroundImageUri.isNotEmpty()

            val file = getItem(position)

            holder.nameText.setTextColor(
                if (dark) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
            )

            holder.descText.setTextColor(
                if (dark) {
                    "#ABABAB".toColorInt()
                } else {
                    "#545454".toColorInt()
                }
            )

            holder.nameText.text = file.name

            holder.descText.text =
                formatFileInfo(file)

            holder.imageViewThumbnail.setImageResource(
                R.drawable.just_a_blank
            )

            holder.imageViewThumbnail.setBackgroundColor(
                if (dark) {
                    "#202020".toColorInt()
                } else {
                    "#E0E0E0".toColorInt()
                }
            )

            val openDrawable = GradientDrawable().apply {
                cornerRadius = 30f

                setColor(
                    "#38A181".toColorInt()
                )

                setStroke(
                    1,
                    "#38A181".toColorInt()
                )
            }

            holder.linearView.background =
                openDrawable

            findButtonText(
                holder.linearView
            )?.text =
                ResourceManagerEx.getStringOrEmpty(
                    App.appContext,
                    R.string.context_menu_logs_viewer_view
                )

            val bgDrawable = GradientDrawable().apply {
                cornerRadii = floatArrayOf(
                    30f,
                    30f,
                    30f,
                    30f,
                    30f,
                    30f,
                    30f,
                    30f
                )

                if (dark) {
                    if (useTransparent) {
                        setColor(
                            "#66111111".toColorInt()
                        )

                        setStroke(
                            2,
                            "#19070707".toColorInt()
                        )
                    } else {
                        setColor(
                            "#111111".toColorInt()
                        )

                        setStroke(
                            2,
                            "#070707".toColorInt()
                        )
                    }
                } else {
                    if (useTransparent) {
                        setColor(
                            "#66F0F3F5".toColorInt()
                        )

                        setStroke(
                            2,
                            "#19EAEDEF".toColorInt()
                        )
                    } else {
                        setColor(
                            "#F0F3F5".toColorInt()
                        )

                        setStroke(
                            2,
                            "#EAEDEF".toColorInt()
                        )
                    }
                }
            }

            holder.linearHey.background =
                bgDrawable

            holder.linearHey.clipToOutline = true

            holder.imageViewThumbnail.clipToOutline = true

            holder.imageViewThumbnail.outlineProvider =
                ViewOutlineProvider.BACKGROUND

            holder.linearView.setOnClickListener {
                if (isAllowedFile(file)) {
                    openFile(file)
                }
            }

            view.setOnClickListener {
                if (isAllowedFile(file)) {
                    openFile(file)
                }
            }

            return view
        }

        private fun isAllowedFile(
            file: File
        ): Boolean {
            return try {
                val parentCanonical =
                    file.parentFile?.canonicalPath

                val allowedCanonical =
                    allowedLogDir.canonicalPath

                file.isFile &&
                        file.extension.equals(
                            "log",
                            ignoreCase = true
                        ) &&
                        parentCanonical == allowedCanonical
            } catch (_: Exception) {
                false
            }
        }

        private fun openFile(
            file: File
        ) {
            try {
                val content =
                    FileTool.safeRead(file)

                val settingsActivity =
                    App.savedSettingsActivity

                if (settingsActivity is SettingsActivity) {
                    settingsActivity.movePage(
                        "LogViewer",
                        content
                    )

                    App.logger.writeLine(
                        "LogViewer::openFile",
                        "Moving page to LogViewer"
                    )
                } else {
                    App.logger.writeLine(
                        "LogExplorerFragment::openFile",
                        "Settings activity not found"
                    )
                }
            } catch (e: Exception) {
                App.logger.writeException(
                    "LogExplorerFragment::openFile",
                    e
                )
            }
        }

        private fun findButtonText(
            view: View
        ): TextView? {
            if (view is TextView) {
                return view
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val result =
                        findButtonText(
                            view.getChildAt(i)
                        )

                    if (result != null) {
                        return result
                    }
                }
            }

            return null
        }

        private fun formatFileInfo(file: File): String {
            return DateFormat
                .getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT,
                    Locale.getDefault()
                )
                .format(Date(file.lastModified()))
        }
    }

    data class ViewHolder(
        val linearHey: LinearLayout,
        val linearView: LinearLayout,
        val imageViewThumbnail: ImageView,
        val nameText: TextView,
        val descText: TextView
    )

    class LogContentFragment : DialogFragment() {

        private lateinit var container: LinearLayout
        private lateinit var titleView: TextView
        private lateinit var contentView: TextView

        override fun onCreateView(
            inflater: LayoutInflater,
            parent: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val context = requireContext()

            container = LinearLayout(
                context
            ).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    25,
                    25,
                    25,
                    25
                )
            }

            titleView = TextView(
                context
            ).apply {
                text = requireArguments()
                    .getString(ARG_FILE_NAME)

                textSize = 18f

                typeface =
                    Typeface.DEFAULT_BOLD

                setPadding(
                    0,
                    0,
                    0,
                    20
                )
            }

            contentView = TextView(
                context
            ).apply {
                text = requireArguments()
                    .getString(ARG_CONTENT)

                textSize = 12f

                typeface =
                    Typeface.MONOSPACE

                setTextIsSelectable(true)

                setPadding(
                    15,
                    15,
                    15,
                    15
                )

                setHorizontallyScrolling(true)
            }

            container.addView(
                titleView,
                LinearLayout.LayoutParams(
                    -1,
                    -2
                )
            )

            container.addView(
                contentView,
                LinearLayout.LayoutParams(
                    -1,
                    0,
                    1f
                )
            )

            applyTheme()

            return container
        }

        override fun onCreateDialog(
            savedInstanceState: Bundle?
        ): Dialog {
            return super.onCreateDialog(
                savedInstanceState
            ).apply {
                window?.setBackgroundDrawable(
                    Color.TRANSPARENT.toDrawable()
                )
            }
        }

        private fun applyTheme() {
            val dark =
                App.config.data.appThemeInApp == "dark"

            container.background =
                GradientDrawable().apply {
                    cornerRadius = 25f

                    setColor(
                        if (dark) {
                            "#080808".toColorInt()
                        } else {
                            "#EFEFEF".toColorInt()
                        }
                    )

                    setStroke(
                        2,
                        if (dark) {
                            "#151515".toColorInt()
                        } else {
                            "#E0E0E0".toColorInt()
                        }
                    )
                }

            titleView.setTextColor(
                if (dark) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
            )

            contentView.setTextColor(
                if (dark) {
                    "#D0D0D0".toColorInt()
                } else {
                    "#202020".toColorInt()
                }
            )

            contentView.background =
                GradientDrawable().apply {
                    cornerRadius = 10f

                    setColor(
                        if (dark) {
                            "#101010".toColorInt()
                        } else {
                            "#FFFFFF".toColorInt()
                        }
                    )
                }
        }

        companion object {
            private const val ARG_FILE_NAME =
                "file_name"

            private const val ARG_CONTENT =
                "content"
        }
    }

    private fun addButton(
        name: String,
        parent: LinearLayout
    ): View? {
        val context = context ?: return null

        val buttonResult: SmallButtonResult =
            ComponentUtils.addSmallButton(
                context,
                name,
                parent
            )

        buttonResult.buttonOne?.setPadding(
            buttonResult.buttonOne.paddingLeft + 100,
            buttonResult.buttonOne.paddingTop,
            buttonResult.buttonOne.paddingRight + 100,
            buttonResult.buttonOne.paddingBottom
        )

        if (buttonResult.buttonView?.parent == null) {
            parent.addView(
                buttonResult.buttonView
            )
        }

        return buttonResult.buttonOne
    }
}
