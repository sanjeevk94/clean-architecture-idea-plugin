package com.korina.myapp.data.local.dao

import androidx.room.*
import com.korina.myapp.data.local.entity.ItemEntity

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    suspend fun getAll(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE itemId = :itemId LIMIT 1")
    suspend fun getById(itemId: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ItemEntity)

    @Delete
    suspend fun delete(entity: ItemEntity)
}
