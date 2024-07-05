package com.witvpn.ikev2.presentation.di

import com.witwork.core_networking.INetworkHelper
import com.witwork.core_networking.NetworkHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideINetworkHelper(): INetworkHelper {
        return NetworkHelper()
    }
}