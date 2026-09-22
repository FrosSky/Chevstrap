package com.chevstrap.rbx.models.apis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class IPInfoResponse(
    @SerialName("city")
    val city: String? = null,

    @SerialName("country")
    val country: String? = null,

    @SerialName("region")
    val region: String? = null
)