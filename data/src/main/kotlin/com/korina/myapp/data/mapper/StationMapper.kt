package com.korina.myapp.`data`.mapper

import com.korina.myapp.`data`.local.entity.StationEntity
import com.korina.myapp.`data`.remote.dto.StationDto
import com.korina.myapp.domain.model.StationModel

public fun StationDto.toDomain(): StationModel = StationModel(
    stationId = stationId,
    stationName = stationName,
    stationNumber = stationNumber
)

public fun StationEntity.toDomain(): StationModel = StationModel(
    stationId = stationId,
    stationName = stationName,
    stationNumber = stationNumber
)

public fun StationModel.toEntity(): StationEntity = StationEntity(
    stationId = stationId,
    stationName = stationName,
    stationNumber = stationNumber
)
