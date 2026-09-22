package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailRequest(
    @SerialName("requestId")
    var requestId: String? = null,

    @SerialName("targetId")
    val targetId: Long,

    @SerialName("type")
    val type: String = "Avatar",

    @SerialName("size")
    val size: String = "30x30",

    @SerialName("format")
    val format: String = "Png",

    @SerialName("isCircular")
    val isCircular: Boolean = true
)