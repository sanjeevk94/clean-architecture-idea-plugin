package com.korina.myapp.`data`.mapper

import com.korina.myapp.`data`.local.entity.ProductEntity
import com.korina.myapp.`data`.remote.dto.ProductDto
import com.korina.myapp.domain.model.ProductModel

public fun ProductDto.toDomain(): ProductModel = ProductModel(
    id = id,
    title = title,
    description = description,
    price = price
)

public fun ProductEntity.toDomain(): ProductModel = ProductModel(
    id = id,
    title = title,
    description = description,
    price = price
)

public fun ProductModel.toEntity(): ProductEntity = ProductEntity(
    id = id,
    title = title,
    description = description,
    price = price
)
