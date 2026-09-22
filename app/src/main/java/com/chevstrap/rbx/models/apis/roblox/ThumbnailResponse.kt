package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailResponse(
    @SerialName("requestId")
    val requestId: String? = null,

    @SerialName("errorCode")
    val errorCode: Int = 0,

    @SerialName("errorMessage")
    val errorMessage: String? = null,

    @SerialName("targetId")
    val targetId: Long,

    /// Valid states:
    /// - Error
    /// - Completed
    /// - InReview
    /// - Pending
    /// - Blocked
    /// - TemporarilyUnavailable

    @SerialName("state")
    val state: String? = null,

    @SerialName("imageUrl")
    val imageUrl: String? = null
)