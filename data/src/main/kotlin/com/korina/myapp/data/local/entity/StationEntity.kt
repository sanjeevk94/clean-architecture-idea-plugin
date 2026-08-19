package com.korina.myapp.`data`.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.String

@Entity(tableName = "Stations")
public data class StationEntity(
  @PrimaryKey
  @ColumnInfo(name = "station_id")
  public val stationId: String,
  @ColumnInfo(name = "station_name")
  public val stationName: String,
  @ColumnInfo(name = "station_number")
  public val stationNumber: String,
)
