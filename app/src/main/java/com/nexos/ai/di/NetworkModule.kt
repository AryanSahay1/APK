package com.nexos.ai.di

import com.nexos.ai.BuildConfig
import com.nexos.ai.data.remote.api.GNewsApi
import com.nexos.ai.data.remote.api.OpenMeteoGeocodingApi
import com.nexos.ai.data.remote.api.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
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
                // Our own redacting logger replaces HttpLoggingInterceptor.BASIC entirely
                // so query params like ?apikey=… never reach Logcat. SKILL-1.md pins us at
                // OkHttp 4.11.0, which predates HttpLoggingInterceptor.redactQueryParams.
                addInterceptor(RedactingLogger(setOf("apikey", "api_key", "key", "token", "access_token")))
            }
        }
        .build()

    @Provides
    @Singleton
    @javax.inject.Named("weatherForecast")
    fun provideWeatherRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(WeatherApi.FORECAST_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideWeatherApi(@javax.inject.Named("weatherForecast") retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    @javax.inject.Named("weatherGeocoding")
    fun provideGeocodingRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(OpenMeteoGeocodingApi.GEOCODING_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGeocodingApi(@javax.inject.Named("weatherGeocoding") retrofit: Retrofit): OpenMeteoGeocodingApi =
        retrofit.create(OpenMeteoGeocodingApi::class.java)

    @Provides
    @Singleton
    @javax.inject.Named("gnews")
    fun provideGNewsRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(GNewsApi.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGNewsApi(@javax.inject.Named("gnews") retrofit: Retrofit): GNewsApi =
        retrofit.create(GNewsApi::class.java)
}

/**
 * Custom request/response logger that redacts query-parameter values for the names in
 * [sensitiveQueryParams] before writing anything to Logcat.
 *
 * Logs:
 *   --> GET https://gnews.io/api/v4/top-headlines?apikey=***&category=technology (28ms 320b)
 *
 * Always passes the original (un-redacted) request through to the network, so the actual
 * API call works correctly. Only the log line is sanitised.
 */
class RedactingLogger(
    private val sensitiveQueryParams: Set<String>,
    private val tag: String = "NexOS/Http"
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.nanoTime()
        val response = try {
            chain.proceed(request)
        } catch (t: Throwable) {
            android.util.Log.w(tag, "← ${request.method} ${redact(request.url)} FAILED: ${t.message}")
            throw t
        }
        val durationMs = (System.nanoTime() - start) / 1_000_000
        val length = response.body?.contentLength()?.takeIf { it > 0 }?.let { "${it}b" } ?: "?b"
        android.util.Log.d(
            tag,
            "${request.method} ${redact(request.url)} -> ${response.code} ($durationMs ms, $length)"
        )
        return response
    }

    private fun redact(url: HttpUrl): String {
        if (url.queryParameterNames.none { name ->
                sensitiveQueryParams.any { it.equals(name, ignoreCase = true) }
            }) return url.toString()
        val builder = url.newBuilder().query(null)
        url.queryParameterNames.forEach { name ->
            val raw = url.queryParameter(name).orEmpty()
            val out = if (sensitiveQueryParams.any { it.equals(name, ignoreCase = true) }) "***" else raw
            builder.addQueryParameter(name, out)
        }
        return builder.build().toString()
    }
}
