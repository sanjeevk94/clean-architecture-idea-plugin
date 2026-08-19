package com.korina.myapp.di

import com.korina.myapp.`data`.repository.ProductRepositoryImpl
import com.korina.myapp.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class ProductModule {
  @Binds
  @Singleton
  public abstract fun bindProductRepository(`impl`: ProductRepositoryImpl): ProductRepository
}
