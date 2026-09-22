package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.Serializable

@Serializable
data class ApiArrayResponse<T>(
    val data: List<T> = emptyList()
)