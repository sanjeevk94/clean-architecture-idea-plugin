package com.korina.myapp.data.repository

import com.korina.myapp.data.local.dao.ItemDao
import com.korina.myapp.data.mapper.toDomain
import com.korina.myapp.data.mapper.toEntity
import com.korina.myapp.data.remote.service.ItemApiService
import com.korina.myapp.domain.model.ItemModel
import com.korina.myapp.domain.repository.ItemRepository

class ItemRepositoryImpl(
    private val dao: ItemDao,
    private val apiService: ItemApiService,
) : ItemRepository {
    override suspend fun getItems(): List<ItemModel> = apiService.getItems().map { it.toDomain() }
}
