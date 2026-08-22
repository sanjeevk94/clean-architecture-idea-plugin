package com.korina.myapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_name")
    val itemName: String?,
    @SerialName("item_code")
    val itemCode: String?,
    val color: String?,
)
