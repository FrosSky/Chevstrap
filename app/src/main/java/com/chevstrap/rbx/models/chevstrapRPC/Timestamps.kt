package com.chevstrap.rbx.models.chevstrapRPC

import kotlinx.serialization.Serializable

@Serializable
data class Timestamps(
    val start: Long? = null,
    val end: Long? = null
)