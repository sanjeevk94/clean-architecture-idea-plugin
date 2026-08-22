package com.korina.myapp.data.mapper

import com.korina.myapp.data.local.entity.ItemEntity
import com.korina.myapp.data.remote.dto.ItemDto
import com.korina.myapp.domain.model.ItemModel


fun ItemDto.toDomain(): ItemModel = ItemModel(
    itemId = itemId,
    itemName = itemName,
    itemCode = itemCode,
    color = color
)

fun ItemEntity.toDomain(): ItemModel = ItemModel(
    itemId = itemId,
    itemName = itemName,
    itemCode = itemCode,
    color = color
)

fun ItemModel.toEntity(): ItemEntity = ItemEntity(
    itemId = itemId,
    itemName = itemName,
    itemCode = itemCode,
    color = color
)
