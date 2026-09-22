package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailBatchResponse(
    @SerialName("data")
    val data: List<ThumbnailResponse> = emptyList()
)