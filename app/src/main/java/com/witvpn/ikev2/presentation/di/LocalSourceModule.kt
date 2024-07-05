package com.witvpn.ikev2.presentation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.strongswan.android.data.VpnProfileDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalSourceModule {

    @Singleton
    @Provides
    fun provideVpnProfileDataSource(@ApplicationContext context: Context): VpnProfileDataSource {
        return VpnProfileDataSource(context)
    }
}