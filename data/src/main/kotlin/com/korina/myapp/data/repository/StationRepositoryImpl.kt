package com.korina.myapp.`data`.repository

import com.korina.myapp.`data`.local.dao.StationDao
import com.korina.myapp.data.mapper.toDomain
import com.korina.myapp.`data`.remote.service.StationApiService
import com.korina.myapp.domain.model.StationModel
import com.korina.myapp.domain.repository.StationRepository
import kotlin.collections.List

public class StationRepositoryImpl(
  private val dao: StationDao,
  private val apiService: StationApiService,
) : StationRepository {
  override suspend fun GetStations(): List<StationModel> = apiService.GetStations().map { it.toDomain() }
}
