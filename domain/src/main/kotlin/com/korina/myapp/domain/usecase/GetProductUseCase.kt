package com.korina.myapp.domain.usecase

import com.korina.myapp.domain.model.ProductModel
import com.korina.myapp.domain.repository.ProductRepository
import kotlin.Long

public class GetProductUseCase(
  private val repository: ProductRepository,
) {
  public suspend operator fun invoke(id: Long): ProductModel? = repository.getProduct(id)
}
