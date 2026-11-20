package com.example

import com.example.domain.CalculatorsEngine
import com.example.domain.ConverterCategory
import com.example.domain.EvalResult
import com.example.domain.MathEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class LkCalculatorUnitTest {

    @Test
    fun `basic arithmetic and smart percent evaluate accurately`() {
        val add = MathEngine.evaluate("12 + 5", decimalPlaces = 2) as EvalResult.Success
        assertEquals("17", add.formatted)

        val percentOf = MathEngine.evaluate("200 × 15%", decimalPlaces = 2) as EvalResult.Success
        assertEquals("30", percentOf.formatted)

        val addPercent = MathEngine.evaluate("100 + 10%", decimalPlaces = 2) as EvalResult.Success
        assertEquals("110", addPercent.formatted)
    }

    @Test
    fun `scientific functions evaluate real formulas accurately`() {
        val sin30 = MathEngine.evaluate("sin(30)", isDegreeMode = true, decimalPlaces = 2) as EvalResult.Success
        assertEquals("0.5", sin30.formatted)

        val cos0 = MathEngine.evaluate("cos(0)", isDegreeMode = true, decimalPlaces = 2) as EvalResult.Success
        assertEquals("1", cos0.formatted)

        val tan45 = MathEngine.evaluate("tan(45)", isDegreeMode = true, decimalPlaces = 2) as EvalResult.Success
        assertEquals("1", tan45.formatted)

        val sqrt81 = MathEngine.evaluate("√(81)", decimalPlaces = 2) as EvalResult.Success
        assertEquals("9", sqrt81.formatted)

        val power = MathEngine.evaluate("2^5", decimalPlaces = 2) as EvalResult.Success
        assertEquals("32", power.formatted)

        val fact = MathEngine.evaluate("5!", decimalPlaces = 2) as EvalResult.Success
        assertEquals("120", fact.formatted)

        val log100 = MathEngine.evaluate("log(100)", decimalPlaces = 2) as EvalResult.Success
        assertEquals("2", log100.formatted)

        val lnE = MathEngine.evaluate("ln(e)", decimalPlaces = 2) as EvalResult.Success
        assertEquals("1", lnE.formatted)

        val reciprocal = MathEngine.evaluate("1÷(4)", decimalPlaces = 2) as EvalResult.Success
        assertEquals("0.25", reciprocal.formatted)

        val piImplicit = MathEngine.evaluate("2π", decimalPlaces = 4) as EvalResult.Success
        assertEquals("6.2832", piImplicit.formatted)
    }

    @Test
    fun `division by zero returns error safely`() {
        val res = MathEngine.evaluate("10÷0")
        assertTrue(res is EvalResult.Error)
    }

    @Test
    fun `unit and currency conversions compute correctly`() {
        val kmToM = CalculatorsEngine.convertUnit(
            ConverterCategory.LENGTH, 2.5, "km", "m", emptyMap()
        )
        assertEquals(2500.0, kmToM, 0.001)

        val cToF = CalculatorsEngine.convertUnit(
            ConverterCategory.TEMPERATURE, 100.0, "°C", "°F", emptyMap()
        )
        assertEquals(212.0, cToF, 0.001)

        val rates = mapOf("USD" to 1.0, "PKR" to 278.0, "INR" to 84.0)
        val usdToPkr = CalculatorsEngine.convertUnit(
            ConverterCategory.CURRENCY, 10.0, "USD", "PKR", rates
        )
        assertEquals(2780.0, usdToPkr, 0.001)
    }

    @Test
    fun `age calculator computes exact years months days`() {
        val today = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(2026, Calendar.NOVEMBER, 20, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val age = CalculatorsEngine.calculateAge(2000, Calendar.JANUARY, 1, today)
        assertNotNull(age)
        assertEquals(26, age!!.years)
        assertEquals(10, age.months)
        assertEquals(19, age.days)
    }

    @Test
    fun `bmi and discount calculators return accurate results`() {
        val bmi = CalculatorsEngine.calculateBmi(weightKg = 70.0, heightCm = 175.0)
        assertNotNull(bmi)
        assertEquals(22.86, bmi!!.bmi, 0.05)
        assertEquals(CalculatorsEngine.BmiStatus.NORMAL, bmi.status)

        val discount = CalculatorsEngine.calculateDiscount(
            originalPrice = 1000.0,
            discountPercent = 25.0,
            taxPercent = 0.0
        )
        assertNotNull(discount)
        assertEquals(250.0, discount!!.discountAmount, 0.001)
        assertEquals(750.0, discount.finalPrice, 0.001)
    }
}
