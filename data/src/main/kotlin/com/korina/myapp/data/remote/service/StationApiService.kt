package com.korina.myapp.`data`.remote.service

import com.korina.myapp.`data`.remote.dto.StationDto
import kotlin.collections.List
import retrofit2.http.GET

public interface StationApiService {
  @GET("/stations")
  public suspend fun GetStations(): List<StationDto>
}
