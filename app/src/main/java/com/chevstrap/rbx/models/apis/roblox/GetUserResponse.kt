package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserResponse(
    @SerialName("description")
    val description: String? = null,

    @SerialName("created")
    val created: String,

    @SerialName("isBanned")
    val isBanned: Boolean,

    @SerialName("externalAppDisplayName")
    val externalAppDisplayName: String? = null,

    @SerialName("hasVerifiedBadge")
    val hasVerifiedBadge: Boolean,

    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String? = null,

    @SerialName("displayName")
    val displayName: String? = null
)