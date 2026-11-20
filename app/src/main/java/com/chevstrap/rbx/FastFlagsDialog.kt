package com.chevstrap.rbx

import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

object FastFlagsDialog {

    fun show(
        activity: Activity?,
        onImport: () -> Unit,
        onExport: () -> Unit
    ) {
        val act = activity ?: return
        val root = LinearLayout(act).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
        }

        val notice = TextView(act).apply {
            text = act.getString(R.string.fast_flags_storage_notice)
        }
        root.addView(notice)

        val actions = LinearLayout(act).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val importButton = Button(act).apply {
            text = act.getString(R.string.fast_flags_import)
        }
        val exportButton = Button(act).apply {
            text = act.getString(R.string.fast_flags_export)
        }
        actions.addView(
            importButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        actions.addView(
            exportButton,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        root.addView(actions)

        val listLayout = LinearLayout(act).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(listLayout)

        val nameInput = EditText(act).apply {
            hint = act.getString(R.string.fast_flags_name_hint)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val valueInput = EditText(act).apply {
            hint = act.getString(R.string.fast_flags_value_hint)
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        root.addView(nameInput)
        root.addView(valueInput)

        fun refresh() {
            listLayout.removeAllViews()
            try {
                val json = FastFlagsManager.load()
                json.keys().asSequence().toList().sorted().forEach { key ->
                    val row = TextView(act).apply {
                        text = act.getString(
                            R.string.fast_flags_row,
                            key,
                            json.get(key).toString()
                        )
                        setPadding(0, 16, 0, 16)
                        setOnClickListener {
                            try {
                                FastFlagsManager.remove(key)
                                refresh()
                            } catch (e: Exception) {
                                showError(act, e)
                            }
                        }
                    }
                    listLayout.addView(row)
                }
            } catch (e: Exception) {
                showError(act, e)
            }
        }
        refresh()

        val dialog = AlertDialog.Builder(act)
            .setTitle(R.string.fast_flags_title)
            .setView(ScrollView(act).apply { addView(root) })
            .setPositiveButton(R.string.common_save) { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(
                        act,
                        R.string.fast_flags_name_required,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    try {
                        FastFlagsManager.set(name, valueInput.text.toString().trim())
                    } catch (e: Exception) {
                        showError(act, e)
                    }
                }
            }
            .setNegativeButton(R.string.common_close, null)
            .create()

        importButton.setOnClickListener {
            dialog.dismiss()
            onImport()
        }
        exportButton.setOnClickListener {
            dialog.dismiss()
            onExport()
        }
        dialog.show()
    }

    private fun showError(activity: Activity, exception: Exception) {
        App.logger.writeException("FastFlagsDialog", exception)
        Toast.makeText(
            activity,
            activity.getString(
                R.string.fast_flags_operation_failed,
                exception.message ?: exception.javaClass.simpleName
            ),
            Toast.LENGTH_LONG
        ).show()
    }
}
