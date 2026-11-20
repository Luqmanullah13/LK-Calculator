package com.example.domain

import java.util.Calendar
import kotlin.math.pow

enum class ConverterCategory(val title: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temp"),
    CURRENCY("Currency")
}

data class UnitItem(
    val code: String,
    val name: String,
    val toBaseFactor: Double = 1.0
)

object UnitCatalog {
    val lengthUnits = listOf(
        UnitItem("mm", "Millimeter", 0.001),
        UnitItem("cm", "Centimeter", 0.01),
        UnitItem("m", "Meter", 1.0),
        UnitItem("km", "Kilometer", 1000.0),
        UnitItem("in", "Inch", 0.0254),
        UnitItem("ft", "Foot", 0.3048),
        UnitItem("yd", "Yard", 0.9144),
        UnitItem("mi", "Mile", 1609.344)
    )

    val weightUnits = listOf(
        UnitItem("mg", "Milligram", 0.001),
        UnitItem("g", "Gram", 1.0),
        UnitItem("kg", "Kilogram", 1000.0),
        UnitItem("oz", "Ounce", 28.349523125),
        UnitItem("lb", "Pound", 453.59237),
        UnitItem("ton", "Metric Ton", 1_000_000.0)
    )

    val temperatureUnits = listOf(
        UnitItem("°C", "Celsius"),
        UnitItem("°F", "Fahrenheit"),
        UnitItem("K", "Kelvin")
    )

    val currencyUnits = listOf(
        UnitItem("USD", "US Dollar ($)"),
        UnitItem("PKR", "Pakistani Rupee (₨)"),
        UnitItem("INR", "Indian Rupee (₹)"),
        UnitItem("EUR", "Euro (€)"),
        UnitItem("GBP", "British Pound (£)"),
        UnitItem("AED", "UAE Dirham (د.إ)"),
        UnitItem("SAR", "Saudi Riyal (﷼)")
    )

    fun unitsFor(category: ConverterCategory): List<UnitItem> = when (category) {
        ConverterCategory.LENGTH -> lengthUnits
        ConverterCategory.WEIGHT -> weightUnits
        ConverterCategory.TEMPERATURE -> temperatureUnits
        ConverterCategory.CURRENCY -> currencyUnits
    }
}

object CalculatorsEngine {

    fun convertUnit(
        category: ConverterCategory,
        value: Double,
        fromCode: String,
        toCode: String,
        currencyRatesFromUsd: Map<String, Double>
    ): Double {
        if (fromCode == toCode) return value
        return when (category) {
            ConverterCategory.LENGTH -> {
                val from = UnitCatalog.lengthUnits.first { it.code == fromCode }
                val to = UnitCatalog.lengthUnits.first { it.code == toCode }
                (value * from.toBaseFactor) / to.toBaseFactor
            }
            ConverterCategory.WEIGHT -> {
                val from = UnitCatalog.weightUnits.first { it.code == fromCode }
                val to = UnitCatalog.weightUnits.first { it.code == toCode }
                (value * from.toBaseFactor) / to.toBaseFactor
            }
            ConverterCategory.TEMPERATURE -> {
                val celsius = when (fromCode) {
                    "°C" -> value
                    "°F" -> (value - 32.0) * 5.0 / 9.0
                    "K" -> value - 273.15
                    else -> value
                }
                when (toCode) {
                    "°C" -> celsius
                    "°F" -> (celsius * 9.0 / 5.0) + 32.0
                    "K" -> celsius + 273.15
                    else -> celsius
                }
            }
            ConverterCategory.CURRENCY -> {
                val fromRate = currencyRatesFromUsd[fromCode] ?: 1.0
                val toRate = currencyRatesFromUsd[toCode] ?: 1.0
                val inUsd = value / fromRate
                inUsd * toRate
            }
        }
    }

    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val totalWeeks: Long,
        val daysUntilNextBirthday: Int
    )

    fun calculateAge(
        birthYear: Int,
        birthMonthZeroIndexed: Int,
        birthDay: Int,
        todayMillis: Long = System.currentTimeMillis()
    ): AgeResult? {
        val today = Calendar.getInstance().apply {
            timeInMillis = todayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dob = Calendar.getInstance().apply {
            isLenient = false
            set(birthYear, birthMonthZeroIndexed, birthDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Trigger validation
        try {
            dob.timeInMillis
        } catch (_: Exception) {
            return null
        }

        if (dob.after(today)) return null

        var years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months -= 1
            val prevMonth = (today.clone() as Calendar).apply {
                add(Calendar.MONTH, -1)
            }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }

        val diffMillis = today.timeInMillis - dob.timeInMillis
        val totalDays = (diffMillis / (1000L * 60L * 60L * 24L)).coerceAtLeast(0L)
        val totalWeeks = totalDays / 7L

        val nextBirthday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, birthMonthZeroIndexed)
            val maxDayThisYear = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, birthDay.coerceAtMost(maxDayThisYear))
            if (!after(today)) {
                add(Calendar.YEAR, 1)
                val maxDayNextYear = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, birthDay.coerceAtMost(maxDayNextYear))
            }
        }
        val daysUntilNext = ((nextBirthday.timeInMillis - today.timeInMillis) / (1000L * 60L * 60L * 24L)).toInt()

        return AgeResult(
            years = years,
            months = months,
            days = days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            daysUntilNextBirthday = daysUntilNext
        )
    }

    enum class BmiStatus(val label: String, val tip: String) {
        UNDERWEIGHT("Underweight", "Consider a nutrient-rich calorie surplus and strength training."),
        NORMAL("Normal / Healthy", "Great balance! Maintain your regular nutrition and activity."),
        OVERWEIGHT("Overweight", "A slight calorie deficit and regular cardio can help."),
        OBESE("Obese", "Consult a healthcare professional for a tailored fitness plan.")
    }

    data class BmiResult(
        val bmi: Double,
        val status: BmiStatus,
        val healthyMinKg: Double,
        val healthyMaxKg: Double
    )

    fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult? {
        if (weightKg <= 0.0 || heightCm <= 0.0) return null
        val heightM = heightCm / 100.0
        val bmi = weightKg / heightM.pow(2.0)
        val status = when {
            bmi < 18.5 -> BmiStatus.UNDERWEIGHT
            bmi < 25.0 -> BmiStatus.NORMAL
            bmi < 30.0 -> BmiStatus.OVERWEIGHT
            else -> BmiStatus.OBESE
        }
        val minKg = 18.5 * heightM.pow(2.0)
        val maxKg = 24.9 * heightM.pow(2.0)
        return BmiResult(bmi = bmi, status = status, healthyMinKg = minKg, healthyMaxKg = maxKg)
    }

    data class DiscountResult(
        val originalPrice: Double,
        val discountAmount: Double,
        val priceAfterDiscount: Double,
        val taxAmount: Double,
        val finalPrice: Double
    )

    fun calculateDiscount(
        originalPrice: Double,
        discountPercent: Double,
        taxPercent: Double = 0.0
    ): DiscountResult? {
        if (originalPrice < 0.0 || discountPercent < 0.0 || discountPercent > 100.0 || taxPercent < 0.0) {
            return null
        }
        val discountAmount = originalPrice * (discountPercent / 100.0)
        val afterDiscount = originalPrice - discountAmount
        val taxAmount = afterDiscount * (taxPercent / 100.0)
        val finalPrice = afterDiscount + taxAmount
        return DiscountResult(
            originalPrice = originalPrice,
            discountAmount = discountAmount,
            priceAfterDiscount = afterDiscount,
            taxAmount = taxAmount,
            finalPrice = finalPrice
        )
    }
}
