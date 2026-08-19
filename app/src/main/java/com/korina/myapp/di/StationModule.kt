package com.korina.myapp.di

import com.korina.myapp.`data`.repository.StationRepositoryImpl
import com.korina.myapp.domain.repository.StationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class StationModule {
  @Binds
  @Singleton
  public abstract fun bindStationRepository(`impl`: StationRepositoryImpl): StationRepository
}
