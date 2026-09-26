package com.chevstrap.rbx.ui.views.customDialogs

import android.app.Dialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.extensions.ResourceManagerEx
import com.chevstrap.rbx.models.entities.ActivityData
import com.chevstrap.rbx.ui.components.ComponentUtils
import com.chevstrap.rbx.ui.components.holders.SmallButtonResult
import com.chevstrap.rbx.ui.viewModels.customDialogs.ServerHistoryViewModel
import java.io.IOException
import java.net.URL

class ServerHistoryFragment : DialogFragment() {
    private lateinit var viewModel: ServerHistoryViewModel

    private lateinit var linear1: LinearLayout

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[
            ServerHistoryViewModel::class.java
        ]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(
                R.layout.server_history_fragment,
                container,
                false
            )

            initialize(view)
            initializeLogic()

            view
        } catch (e: Exception) {
            App.logger.writeException(
                "ServerHistoryFragment",
                e
            )
            null
        }
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(
                Color.TRANSPARENT.toDrawable()
            )
        }
    }

    private fun initialize(view: View) {

        val listView =
            view.findViewById<ListView>(R.id.listview)

        linear1 =
            view.findViewById(R.id.linear1)

        val title =
            view.findViewById<TextView>(
                R.id.textview_is_title
            )

        val description =
            view.findViewById<TextView>(
                R.id.textview_is_description
            )

        val bottomLayout =
            view.findViewById<LinearLayout>(
                R.id.linear_bottom_list
            )

        title.text =
            ResourceManagerEx.getStringOrEmpty(
                requireContext(),
                R.string.context_menu_game_history_title
            )

        description.text =
            ResourceManagerEx.getStringOrEmpty(
                requireContext(),
                R.string.context_menu_game_history_description
            )

        val buttonClose = addButton(
            ResourceManagerEx.getStringOrEmpty(
                requireContext(),
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

        description.setTextColor(
            if (light) {
                Color.parseColor("#E0E0E0")
            } else {
                Color.parseColor("#202020")
            }
        )

        val listBackground = GradientDrawable().apply {
            cornerRadius = 20f
            setColor("#070707".toColorInt())
        }

        listView.background = listBackground

        try {
            viewModel.loadData()

            listView.adapter =
                HistoryAdapter(viewModel.history)
        } catch (e: Exception) {
            App.logger.writeException(
                "ServerHistoryFragment",
                e
            )
        }
    }

    private fun initializeLogic() {

        val videoUri =
            App.config.data.backgroundImageUri

        val useTransparent =
            videoUri.isNotEmpty()

        val light =
            App.config.data.appThemeInApp == "light"

        val bg = GradientDrawable().apply {
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

        linear1.background = bg
    }

    companion object {

        class HistoryAdapter(
            private val history: List<ActivityData>
        ) : BaseAdapter() {

            override fun getCount(): Int {
                return history.size
            }

            override fun getItem(
                position: Int
            ): ActivityData {
                return history[position]
            }

            override fun getItemId(
                position: Int
            ): Long {
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

                    view =
                        LayoutInflater.from(parent.context)
                            .inflate(
                                R.layout.experience_item_history,
                                parent,
                                false
                            )

                    holder = ViewHolder(
                        linearHey =
                            view.findViewById(
                                R.id.linear_hey
                            ),

                        linearRejoin =
                            view.findViewById(
                                R.id.linear_rejoin
                            ),

                        imageViewThumbnail =
                            view.findViewById(
                                R.id.imageview_thumbnail
                            ),

                        nameText =
                            view.findViewById(
                                R.id.textview_name_option
                            ),

                        descText =
                            view.findViewById(
                                R.id.textview_description_option
                            )
                    )

                    view.tag = holder

                } else {

                    view = convertView

                    holder =
                        convertView.tag as ViewHolder
                }

                val dark =
                    App.config.data.appThemeInApp ==
                            "dark"

                val useTransparent =
                    App.config.data
                        .backgroundImageUri
                        .isNotEmpty()

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

                val data =
                    getItem(position)

                val name =
                    data.universeDetails
                        ?.data
                        ?.name
                        ?: ""

                holder.imageViewThumbnail
                    .setBackgroundColor(
                        "#202020".toColorInt()
                    )

                holder.imageViewThumbnail.tag = null

                val imageUrl =
                    data.universeDetails
                        ?.thumbnail
                        ?.imageUrl

                if (!imageUrl.isNullOrEmpty()) {

                    holder.imageViewThumbnail.tag =
                        imageUrl

                    Thread {

                        try {

                            val bitmap =
                                BitmapFactory.decodeStream(
                                    URL(imageUrl)
                                        .openConnection()
                                        .getInputStream()
                                )

                            val drawable =
                                BitmapDrawable(
                                    parent.context.resources,
                                    bitmap
                                )

                            holder.imageViewThumbnail.post {

                                if (
                                    imageUrl ==
                                    holder.imageViewThumbnail.tag
                                ) {
                                    holder.imageViewThumbnail
                                        .background =
                                        drawable
                                }
                            }

                        } catch (e: IOException) {

                            App.logger.writeException(
                                "ServerHistoryFragment::HistoryAdapter",
                                e
                            )
                        }

                    }.start()
                }

                val rejoinDrawable =
                    GradientDrawable().apply {

                        cornerRadius = 30f

                        setColor(
                            "#38A181".toColorInt()
                        )

                        setStroke(
                            1,
                            "#38A181".toColorInt()
                        )
                    }

                holder.linearRejoin.background =
                    rejoinDrawable

                findButtonText(
                    holder.linearRejoin
                )?.text =
                    ResourceManagerEx.getStringOrEmpty(
                        App.appContext,
                        R.string.context_menu_logs_viewer_view
                    )

                holder.nameText.text =
                    name

                holder.descText.text =
                    data.gameHistoryDescription

                val bgDrawable =
                    GradientDrawable().apply {

                        cornerRadii =
                            floatArrayOf(
                                30f, 30f,
                                30f, 30f,
                                30f, 30f,
                                30f, 30f
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

                holder.linearHey.clipToOutline =
                    true

                holder.imageViewThumbnail
                    .clipToOutline = true

                holder.imageViewThumbnail
                    .outlineProvider =
                    ViewOutlineProvider.BACKGROUND

                holder.linearRejoin.setOnClickListener {

                    data.rejoinServer(
                        parent.context
                    )
                }

                return view
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
        }

        data class ViewHolder(
            val linearHey: LinearLayout,
            val linearRejoin: LinearLayout,
            val imageViewThumbnail: ImageView,
            val nameText: TextView,
            val descText: TextView
        )
    }

    private fun addButton(
        name: String,
        parent: LinearLayout
    ): View? {

        val context =
            context ?: return null

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
