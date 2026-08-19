package com.korina.myapp.`data`.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.korina.myapp.`data`.local.entity.StationEntity
import kotlin.String
import kotlin.collections.List

@Dao
public interface StationDao {
  @Query("SELECT * FROM Stations")
  public suspend fun getAll(): List<StationEntity>

  @Query("SELECT * FROM Stations WHERE stationId = :stationId LIMIT 1")
  public suspend fun getById(stationId: String): StationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(entities: List<StationEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(entity: StationEntity)

  @Delete
  public suspend fun delete(entity: StationEntity)
}
