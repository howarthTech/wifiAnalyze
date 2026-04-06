package com.wifianalyze.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.room.Room
import com.wifianalyze.data.local.RoomReadingDao
import com.wifianalyze.data.local.WifiAnalyzeDatabase
import com.wifianalyze.data.wifi.WifiScanner
import com.wifianalyze.data.wifi.WifiScannerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWifiManager(@ApplicationContext context: Context): WifiManager =
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WifiAnalyzeDatabase =
        Room.databaseBuilder(
            context,
            WifiAnalyzeDatabase::class.java,
            "wifi_analyze.db"
        ).build()

    @Provides
    fun provideRoomReadingDao(database: WifiAnalyzeDatabase): RoomReadingDao =
        database.roomReadingDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindWifiScanner(impl: WifiScannerImpl): WifiScanner
}
