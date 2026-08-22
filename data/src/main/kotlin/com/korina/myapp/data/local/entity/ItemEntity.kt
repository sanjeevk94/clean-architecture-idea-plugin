package com.korina.myapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "item_name")
    val itemName: String?,
    @ColumnInfo(name = "item_code")
    val itemCode: String?,
    @ColumnInfo(name = "color")
    val color: String?,
)
