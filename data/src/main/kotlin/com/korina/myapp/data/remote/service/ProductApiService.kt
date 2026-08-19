package com.korina.myapp.`data`.remote.service

import com.korina.myapp.`data`.remote.dto.ProductDto
import kotlin.Long
import kotlin.collections.List
import retrofit2.http.GET
import retrofit2.http.Path

public interface ProductApiService {
  @GET("/products")
  public suspend fun getProducts(): List<ProductDto>

  @GET("/products/{id}")
  public suspend fun getProduct(@Path("id") id: Long): ProductDto?
}
