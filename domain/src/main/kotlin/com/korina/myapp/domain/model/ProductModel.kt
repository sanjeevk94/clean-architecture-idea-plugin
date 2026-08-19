package com.korina.myapp.domain.model

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class ProductModel(
  public val id: Long,
  public val title: String,
  public val description: String?,
  public val price: Double,
)
