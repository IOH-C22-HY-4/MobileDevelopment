package com.ioh_c22_h2_4.hy_ponics.di

import com.ioh_c22_h2_4.hy_ponics.data.repository.IOTRepository
import com.ioh_c22_h2_4.hy_ponics.data.repository.IOTRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIOTRepository(iotRepositoryImpl: IOTRepositoryImpl): IOTRepository

}