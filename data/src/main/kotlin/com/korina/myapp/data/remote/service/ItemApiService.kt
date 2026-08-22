package com.korina.myapp.data.remote.service

import com.korina.myapp.data.remote.dto.ItemDto
import retrofit2.http.GET

interface ItemApiService {
    @GET("/items")
    suspend fun getItems(): List<ItemDto>
}
