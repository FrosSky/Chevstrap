package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameCreator(
    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("isRNVAccount")
    val isRNVAccount: Boolean,

    @SerialName("hasVerifiedBadge")
    val hasVerifiedBadge: Boolean
)