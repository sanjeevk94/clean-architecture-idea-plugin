package com.korina.myapp.domain.repository

import com.korina.myapp.domain.model.ItemModel

interface ItemRepository {
    suspend fun getItems(): List<ItemModel>
}
