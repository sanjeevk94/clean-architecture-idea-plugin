package com.korina.myapp.`data`.remote.dto

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class StationDto(
  @SerialName("station_id")
  public val stationId: String,
  @SerialName("station_name")
  public val stationName: String,
  @SerialName("station_number")
  public val stationNumber: String,
)
