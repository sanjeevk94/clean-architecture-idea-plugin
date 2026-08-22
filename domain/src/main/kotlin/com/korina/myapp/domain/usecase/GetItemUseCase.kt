package com.korina.myapp.domain.usecase

import com.korina.myapp.domain.model.ItemModel
import com.korina.myapp.domain.repository.ItemRepository

class GetItemUseCase(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(): List<ItemModel> = repository.getItems()
}
