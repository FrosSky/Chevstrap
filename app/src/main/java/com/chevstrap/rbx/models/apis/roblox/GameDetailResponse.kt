package com.chevstrap.rbx.models.apis.roblox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDetailResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("rootPlaceId")
    val rootPlaceId: Long,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("sourceName")
    val sourceName: String? = null,

    @SerialName("sourceDescription")
    val sourceDescription: String? = null,

    @SerialName("creator")
    val creator: GameCreator? = null,

    @SerialName("price")
    val price: Long?,

    @SerialName("allowedGearGenres")
    val allowedGearGenres: List<String>? = null,

    @SerialName("allowedGearCategories")
    val allowedGearCategories: List<String>? = null,

    @SerialName("isGenreEnforced")
    val isGenreEnforced: Boolean,

    @SerialName("copyingAllowed")
    val copyingAllowed: Boolean,

    @SerialName("playing")
    val playing: Long,

    @SerialName("visits")
    val visits: Long,

    @SerialName("maxPlayers")
    val maxPlayers: Int,

    @SerialName("created")
    val created: String,

    @SerialName("updated")
    val updated: String,

    @SerialName("studioAccessToApisAllowed")
    val studioAccessToApisAllowed: Boolean,

    @SerialName("createVipServersAllowed")
    val createVipServersAllowed: Boolean,

    @SerialName("universeAvatarType")
    val universeAvatarType: String? = null,

    @SerialName("genre")
    val genre: String? = null,

    @SerialName("isAllGenre")
    val isAllGenre: Boolean,

    @SerialName("isFavoritedByUser")
    val isFavoritedByUser: Boolean,

    @SerialName("favoritedCount")
    val favoritedCount: Int
)