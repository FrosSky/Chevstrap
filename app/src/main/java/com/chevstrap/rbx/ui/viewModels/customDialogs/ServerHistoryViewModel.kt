package com.chevstrap.rbx.ui.viewModels.customDialogs

import androidx.lifecycle.ViewModel
import com.chevstrap.rbx.CustomWatcher
import com.chevstrap.rbx.models.entities.ActivityData

class ServerHistoryViewModel : ViewModel() {

    private val _history = mutableListOf<ActivityData>()

    val history: List<ActivityData>
        get() = _history

    fun loadData() {
        try {
            _history.clear()

            _history.addAll(
                CustomWatcher.getInstance()
                    .allHistoryServer
            )
        } catch (_: Exception) {
            _history.clear()
        }
    }
}