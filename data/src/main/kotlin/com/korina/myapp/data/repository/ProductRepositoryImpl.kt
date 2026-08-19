package com.korina.myapp.`data`.repository

import com.korina.myapp.`data`.local.dao.ProductDao
import com.korina.myapp.`data`.remote.service.ProductApiService
import com.korina.myapp.domain.model.ProductModel
import com.korina.myapp.domain.repository.ProductRepository
import kotlin.Long
import kotlin.collections.List

public class ProductRepositoryImpl(
  private val dao: ProductDao,
  private val apiService: ProductApiService,
) : ProductRepository {
  override suspend fun getProducts(): List<ProductModel> = apiService.getProducts().map { it.toDomain() }

  override suspend fun getProduct(id: Long): ProductModel? = apiService.getProduct(id)?.toDomain()
}
