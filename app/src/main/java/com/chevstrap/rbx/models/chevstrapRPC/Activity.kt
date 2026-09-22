package com.chevstrap.rbx.models.chevstrapRPC

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val type: Int = 0,

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    val name: String? = null,
    val details: String,
    val state: String,

    @SerialName("application_id")
    val applicationId: String? = null,

    val url: String? = null,
    val instance: Boolean? = null,
    val flags: Int? = null,
    val timestamps: Timestamps? = null,
    val assets: Assets? = null,
    val buttons: List<String>? = null,
    val metadata: Metadata? = null
)