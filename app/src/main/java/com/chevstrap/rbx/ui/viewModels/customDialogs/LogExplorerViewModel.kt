package com.chevstrap.rbx.ui.viewModels.customDialogs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import chevstrap.extensions.FileTool
import com.chevstrap.rbx.App
import com.chevstrap.rbx.Paths
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class LogExplorerViewModel : ViewModel() {

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private val _logFiles =
        MutableLiveData<List<File>>(emptyList())

    val logFiles: LiveData<List<File>> =
        _logFiles

    val logDirectory: File
        get() = File(Paths.logs)

    fun loadData() {
        scope.launch {
            try {
                FileTool.ensureDirectoryExists(
                    logDirectory
                )

                val files =
                    FileTool.listFiles(
                        logDirectory
                    )
                        .filter { file ->
                            file.isFile &&
                                    file.extension.equals(
                                        "log",
                                        ignoreCase = true
                                    )
                        }
                        .sortedByDescending { file ->
                            file.lastModified()
                        }

                _logFiles.postValue(files)
            } catch (e: Exception) {
                App.logger.writeException(
                    "LogExplorerViewModel::loadData",
                    e
                )

                _logFiles.postValue(
                    emptyList()
                )
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}