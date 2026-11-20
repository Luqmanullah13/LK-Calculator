package com.example.data

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    val result: String?,
    val rates: Map<String, Double>?
)

interface CurrencyApiService {
    @GET("v6/latest/USD")
    suspend fun getLatestUsdRates(): ExchangeRateResponse

    companion object {
        fun create(): CurrencyApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://open.er-api.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(CurrencyApiService::class.java)
        }
    }
}

class CurrencyRepository(
    private val apiService: CurrencyApiService = CurrencyApiService.create(),
    private val settingsRepository: SettingsRepository
) {
    suspend fun refreshRates(): Result<String> {
        return try {
            val response = apiService.getLatestUsdRates()
            val rates = response.rates
            if (response.result == "success" && !rates.isNullOrEmpty()) {
                val timeLabel = "Live • " + SimpleDateFormat("HH:mm, dd MMM", Locale.US).format(Date())
                settingsRepository.saveCurrencyRates(rates, timeLabel)
                Result.success(timeLabel)
            } else {
                Result.failure(IllegalStateException("Invalid exchange rate response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
