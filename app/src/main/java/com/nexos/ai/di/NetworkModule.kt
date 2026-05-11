package com.nexos.ai.di

import com.nexos.ai.BuildConfig
import com.nexos.ai.data.remote.api.NewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Retrofit module for non-AI HTTP APIs (NewsAPI today, more later).
 *
 * Defence-in-depth: even though NewsAPI accepts ?apiKey= as a query param, we never use
 * that variant — see [com.nexos.ai.data.remote.api.NewsApi] — and the logger redacts the
 * `X-Api-Key` header below in case any future endpoint slips it in.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                val logger = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                    redactHeader("x-api-key")
                    redactHeader("X-Api-Key")
                    redactHeader("x-goog-api-key")
                }
                addInterceptor(logger)
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideNewsRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(NewsApi.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideNewsApi(retrofit: Retrofit): NewsApi = retrofit.create(NewsApi::class.java)
}
