package com.korina.myapp.domain.repository

import com.korina.myapp.domain.model.StationModel
import kotlin.collections.List

public interface StationRepository {
  public suspend fun GetStations(): List<StationModel>
}
