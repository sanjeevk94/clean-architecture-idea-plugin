package com.korina.myapp.`data`.remote.dto

import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ProductDto(
  @SerialName("id")
  public val id: Long,
  @SerialName("title")
  public val title: String,
  @SerialName("description")
  public val description: String?,
  @SerialName("price")
  public val price: Double,
)
