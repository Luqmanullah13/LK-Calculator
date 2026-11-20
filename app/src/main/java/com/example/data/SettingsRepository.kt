package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lk_calculator_settings")

data class UserSettings(
    val decimalPlaces: Int = 2,
    val vibrateEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val isDegreeMode: Boolean = true,
    val currencyRates: Map<String, Double> = DEFAULT_CURRENCY_RATES,
    val ratesUpdatedLabel: String = "Baseline Rates"
) {
    companion object {
        val DEFAULT_CURRENCY_RATES = mapOf(
            "USD" to 1.0,
            "PKR" to 278.50,
            "INR" to 84.40,
            "EUR" to 0.92,
            "GBP" to 0.79,
            "AED" to 3.6725,
            "SAR" to 3.75
        )
    }
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val VIBRATE_ENABLED = booleanPreferencesKey("vibrate_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DEGREE_MODE = booleanPreferencesKey("degree_mode")
        val RATE_PKR = doublePreferencesKey("rate_pkr")
        val RATE_INR = doublePreferencesKey("rate_inr")
        val RATE_EUR = doublePreferencesKey("rate_eur")
        val RATE_GBP = doublePreferencesKey("rate_gbp")
        val RATE_AED = doublePreferencesKey("rate_aed")
        val RATE_SAR = doublePreferencesKey("rate_sar")
        val RATES_UPDATED = stringPreferencesKey("rates_updated")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val defaults = UserSettings.DEFAULT_CURRENCY_RATES
        val rates = mapOf(
            "USD" to 1.0,
            "PKR" to (prefs[Keys.RATE_PKR] ?: defaults.getValue("PKR")),
            "INR" to (prefs[Keys.RATE_INR] ?: defaults.getValue("INR")),
            "EUR" to (prefs[Keys.RATE_EUR] ?: defaults.getValue("EUR")),
            "GBP" to (prefs[Keys.RATE_GBP] ?: defaults.getValue("GBP")),
            "AED" to (prefs[Keys.RATE_AED] ?: defaults.getValue("AED")),
            "SAR" to (prefs[Keys.RATE_SAR] ?: defaults.getValue("SAR"))
        )
        UserSettings(
            decimalPlaces = prefs[Keys.DECIMAL_PLACES] ?: 2,
            vibrateEnabled = prefs[Keys.VIBRATE_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            isDarkTheme = prefs[Keys.DARK_THEME] ?: true,
            isDegreeMode = prefs[Keys.DEGREE_MODE] ?: true,
            currencyRates = rates,
            ratesUpdatedLabel = prefs[Keys.RATES_UPDATED] ?: "Baseline Rates"
        )
    }

    suspend fun setDecimalPlaces(places: Int) {
        context.dataStore.edit { it[Keys.DECIMAL_PLACES] = places }
    }

    suspend fun setVibrateEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATE_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = isDark }
    }

    suspend fun setDegreeMode(isDegree: Boolean) {
        context.dataStore.edit { it[Keys.DEGREE_MODE] = isDegree }
    }

    suspend fun saveCurrencyRates(rates: Map<String, Double>, updatedLabel: String) {
        context.dataStore.edit { prefs ->
            rates["PKR"]?.let { prefs[Keys.RATE_PKR] = it }
            rates["INR"]?.let { prefs[Keys.RATE_INR] = it }
            rates["EUR"]?.let { prefs[Keys.RATE_EUR] = it }
            rates["GBP"]?.let { prefs[Keys.RATE_GBP] = it }
            rates["AED"]?.let { prefs[Keys.RATE_AED] = it }
            rates["SAR"]?.let { prefs[Keys.RATE_SAR] = it }
            prefs[Keys.RATES_UPDATED] = updatedLabel
        }
    }
}
