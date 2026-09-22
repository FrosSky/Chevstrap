package com.chevstrap.rbx.models.chevstrapRPC

import kotlinx.serialization.Serializable

@Serializable
data class Buttons(
    val items: List<String> = emptyList()
)