package com.korina.myapp.`data`.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.korina.myapp.`data`.local.entity.ProductEntity
import kotlin.Long
import kotlin.collections.List

@Dao
public interface ProductDao {
  @Query("SELECT * FROM products")
  public suspend fun getAll(): List<ProductEntity>

  @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
  public suspend fun getById(id: Long): ProductEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(entities: List<ProductEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(entity: ProductEntity)

  @Delete
  public suspend fun delete(entity: ProductEntity)
}
