package com.korina.myapp.domain.repository

import com.korina.myapp.domain.model.ProductModel
import kotlin.Long
import kotlin.collections.List

public interface ProductRepository {
  public suspend fun getProducts(): List<ProductModel>

  public suspend fun getProduct(id: Long): ProductModel?
}
