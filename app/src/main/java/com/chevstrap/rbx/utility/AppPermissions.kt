package com.chevstrap.rbx.utility

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chevstrap.rbx.App
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

object AppPermissions {
    private val context
        get() = requireNotNull(App.appContext)

    private var pendingRequest: PendingRequest? = null

    private data class PendingRequest(
        val requestCode: Int,
        val continuation: CancellableContinuation<Boolean>
    )

    suspend fun request(
        activity: Activity,
        vararg permissions: String
    ): Boolean {
        val missing = permissions.filterNot(::isGranted)

        if (missing.isEmpty()) {
            return true
        }

        val requestCode = (System.currentTimeMillis() and 0xFFFF).toInt()
        return suspendCancellableCoroutine { continuation ->

            pendingRequest = PendingRequest(
                requestCode,
                continuation
            )

            continuation.invokeOnCancellation {
                if (pendingRequest?.requestCode == requestCode) {
                    pendingRequest = null
                }
            }

            ActivityCompat.requestPermissions(
                activity,
                missing.toTypedArray(),
                requestCode
            )
        }
    }

    fun areDeclined(vararg permissions: String): Boolean {
        return permissions.any(::isDeclined)
    }

    fun isDeclined(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_DENIED
    }

    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun areGranted(vararg permissions: String): Boolean {
        return permissions.all(::isGranted)
    }

}