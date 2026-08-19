package com.korina.myapp.`data`.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.Double
import kotlin.Long
import kotlin.String

@Entity(tableName = "products")
public data class ProductEntity(
  @PrimaryKey
  @ColumnInfo(name = "id")
  public val id: Long,
  @ColumnInfo(name = "title")
  public val title: String,
  @ColumnInfo(name = "description")
  public val description: String?,
  @ColumnInfo(name = "price")
  public val price: Double,
)
