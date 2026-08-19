package com.korina.myapp.domain.usecase

import com.korina.myapp.domain.model.StationModel
import com.korina.myapp.domain.repository.StationRepository
import kotlin.collections.List

public class GetStationsUseCase(
  private val repository: StationRepository,
) {
  public suspend operator fun invoke(): List<StationModel> = repository.GetStations()
}
