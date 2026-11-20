package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculatorDatabase
import com.example.data.CurrencyRepository
import com.example.data.HistoryEntity
import com.example.data.HistoryRepository
import com.example.data.SettingsRepository
import com.example.data.UserSettings
import com.example.domain.CalculatorsEngine
import com.example.domain.ConverterCategory
import com.example.domain.EvalResult
import com.example.domain.MathEngine
import com.example.domain.UnitCatalog
import com.example.util.FeedbackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppDestination(val title: String) {
    CALCULATOR("Calculator"),
    HISTORY("History"),
    CONVERTER("Converter"),
    AGE("Age Calc"),
    BMI("BMI Calc"),
    DISCOUNT("Discount"),
    SETTINGS("Settings")
}

data class CalculatorUiState(
    val currentDestination: AppDestination = AppDestination.CALCULATOR,
    val expression: String = "",
    val livePreview: String = "",
    val lastCalculation: String = "",
    val errorMessage: String? = null,
    val isScientificMode: Boolean = false,
    val justEvaluated: Boolean = false,
    // Converter
    val converterCategory: ConverterCategory = ConverterCategory.CURRENCY,
    val converterInput: String = "1",
    val converterFromUnit: String = "USD",
    val converterToUnit: String = "PKR",
    val converterOutput: String = "278.5",
    val isRefreshingRates: Boolean = false,
    // Age Calc (default sample DOB: 2000-01-01)
    val birthYear: Int = 2000,
    val birthMonthZeroIndexed: Int = 0,
    val birthDay: Int = 1,
    val ageResult: CalculatorsEngine.AgeResult? = null,
    // BMI Calc
    val bmiIsMetric: Boolean = true,
    val bmiWeightInput: String = "70",
    val bmiHeightCmInput: String = "175",
    val bmiHeightFtInput: String = "5",
    val bmiHeightInInput: String = "9",
    val bmiResult: CalculatorsEngine.BmiResult? = null,
    // Discount Calc
    val discountPriceInput: String = "1000",
    val discountPercentInput: String = "20",
    val discountTaxInput: String = "0",
    val discountResult: CalculatorsEngine.DiscountResult? = null
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CalculatorDatabase.getInstance(application)
    private val historyRepository = HistoryRepository(database.historyDao())
    private val settingsRepository = SettingsRepository(application)
    private val currencyRepository = CurrencyRepository(settingsRepository = settingsRepository)
    val feedbackManager = FeedbackManager(application)

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    val historyList: StateFlow<List<HistoryEntity>> = historyRepository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        recalculateAge()
        recalculateBmi()
        recalculateDiscount()

        viewModelScope.launch {
            combine(settings, historyList) { currentSettings, history ->
                currentSettings to history
            }.collect { (currentSettings, history) ->
                val latestHistoryLine = history.firstOrNull()?.let { "${it.expression} = ${it.result}" } ?: ""
                _uiState.update { state ->
                    val updatedPreview = if (state.expression.isNotEmpty() && !state.justEvaluated) {
                        MathEngine.evaluatePreview(
                            expression = state.expression,
                            isDegreeMode = currentSettings.isDegreeMode,
                            decimalPlaces = currentSettings.decimalPlaces
                        ) ?: ""
                    } else {
                        state.livePreview
                    }
                    val updatedConverterOutput = computeConversion(
                        category = state.converterCategory,
                        input = state.converterInput,
                        fromCode = state.converterFromUnit,
                        toCode = state.converterToUnit,
                        currentSettings = currentSettings
                    )
                    state.copy(
                        lastCalculation = if (state.lastCalculation.isEmpty()) latestHistoryLine else state.lastCalculation,
                        livePreview = updatedPreview,
                        converterOutput = updatedConverterOutput
                    )
                }
            }
        }
    }

    private fun triggerHapticAndSound(heavy: Boolean = false) {
        val current = settings.value
        feedbackManager.triggerFeedback(
            vibrate = current.vibrateEnabled,
            sound = current.soundEnabled,
            heavy = heavy
        )
    }

    fun navigateTo(destination: AppDestination) {
        triggerHapticAndSound()
        _uiState.update { it.copy(currentDestination = destination) }
    }

    fun navigateBack() {
        triggerHapticAndSound()
        _uiState.update { it.copy(currentDestination = AppDestination.CALCULATOR) }
    }

    fun toggleScientificMode() {
        triggerHapticAndSound()
        _uiState.update { it.copy(isScientificMode = !it.isScientificMode) }
    }

    fun toggleDarkTheme() {
        triggerHapticAndSound()
        viewModelScope.launch {
            settingsRepository.setDarkTheme(!settings.value.isDarkTheme)
        }
    }

    fun toggleSound() {
        val next = !settings.value.soundEnabled
        feedbackManager.triggerFeedback(vibrate = settings.value.vibrateEnabled, sound = next)
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(next)
        }
    }

    fun toggleVibrate() {
        val next = !settings.value.vibrateEnabled
        feedbackManager.triggerFeedback(vibrate = next, sound = settings.value.soundEnabled)
        viewModelScope.launch {
            settingsRepository.setVibrateEnabled(next)
        }
    }

    fun toggleDegreeMode() {
        triggerHapticAndSound()
        viewModelScope.launch {
            settingsRepository.setDegreeMode(!settings.value.isDegreeMode)
        }
    }

    fun setDecimalPlaces(places: Int) {
        triggerHapticAndSound()
        viewModelScope.launch {
            settingsRepository.setDecimalPlaces(places)
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    // ---- CALCULATOR INPUT ACTIONS ----

    fun onDigitClick(digit: String) {
        triggerHapticAndSound()
        _uiState.update { state ->
            val nextExpr = if (state.justEvaluated) {
                digit
            } else {
                state.expression + digit
            }
            updateExpressionState(state, nextExpr, justEvaluated = false)
        }
    }

    fun onDecimalClick() {
        triggerHapticAndSound()
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            // Prevent multiple dots in the same number segment
            val lastNumberSegment = base.takeLastWhile { it.isDigit() || it == '.' }
            if (lastNumberSegment.contains('.')) return@update state
            val toAppend = if (base.isEmpty() || (!base.last().isDigit() && base.last() != '.')) "0." else "."
            updateExpressionState(state, base + toAppend, justEvaluated = false)
        }
    }

    fun onOperatorClick(op: String) {
        triggerHapticAndSound()
        _uiState.update { state ->
            val base = state.expression
            if (base.isEmpty()) {
                return@update if (op == "-") {
                    updateExpressionState(state, "-", justEvaluated = false)
                } else {
                    state
                }
            }
            val last = base.last()
            val operators = listOf('+', '-', '×', '÷', '^')
            val nextExpr = if (last in operators) {
                if (op == "-" && (last == '×' || last == '÷' || last == '^')) {
                    base + op
                } else if (base.length > 1) {
                    base.dropLast(1) + op
                } else if (op == "-") {
                    "-"
                } else {
                    base
                }
            } else if (last == '(' && op != "-") {
                base
            } else {
                base + op
            }
            updateExpressionState(state, nextExpr, justEvaluated = false)
        }
    }

    fun onPercentClick() {
        triggerHapticAndSound()
        _uiState.update { state ->
            val base = state.expression
            if (base.isEmpty()) return@update state
            val last = base.last()
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '!') {
                updateExpressionState(state, "$base%", justEvaluated = false)
            } else {
                state
            }
        }
    }

    fun onParenthesisClick() {
        triggerHapticAndSound()
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val openCount = base.count { it == '(' }
            val closeCount = base.count { it == ')' }
            val last = base.lastOrNull()
            val insertClose = openCount > closeCount &&
                last != null &&
                (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '%' || last == '!')
            val nextExpr = base + if (insertClose) ")" else "("
            updateExpressionState(state, nextExpr, justEvaluated = false)
        }
    }

    fun onScientificFunctionClick(funcToken: String) {
        triggerHapticAndSound()
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val nextExpr = when (funcToken) {
                "sin", "cos", "tan", "log", "ln" -> "$base$funcToken("
                "√" -> "$base√("
                "π" -> "${base}π"
                "e" -> "${base}e"
                "^" -> {
                    if (base.isEmpty() || base.last() in listOf('+', '-', '×', '÷', '^', '(')) {
                        base
                    } else {
                        "$base^"
                    }
                }
                "!" -> {
                    val last = base.lastOrNull()
                    if (last != null && (last.isDigit() || last == ')')) {
                        "$base!"
                    } else {
                        base
                    }
                }
                "1/x" -> {
                    if (base.isEmpty()) {
                        "1÷("
                    } else if (state.justEvaluated) {
                        "1÷($base)"
                    } else {
                        val trailingNumber = base.takeLastWhile { it.isDigit() || it == '.' }
                        if (trailingNumber.isNotEmpty()) {
                            base.dropLast(trailingNumber.length) + "1÷($trailingNumber)"
                        } else {
                            "${base}1÷("
                        }
                    }
                }
                else -> base
            }
            updateExpressionState(state, nextExpr, justEvaluated = false)
        }
    }

    fun onBackspaceClick() {
        triggerHapticAndSound()
        _uiState.update { state ->
            if (state.expression.isEmpty()) {
                return@update state.copy(errorMessage = null, livePreview = "")
            }
            val expr = state.expression
            val functionSuffixes = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")
            val matchedSuffix = functionSuffixes.firstOrNull { expr.endsWith(it) }
            val nextExpr = if (matchedSuffix != null) {
                expr.dropLast(matchedSuffix.length)
            } else {
                expr.dropLast(1)
            }
            updateExpressionState(state, nextExpr, justEvaluated = false)
        }
    }

    fun onClearClick() {
        triggerHapticAndSound()
        _uiState.update {
            it.copy(
                expression = "",
                livePreview = "",
                errorMessage = null,
                justEvaluated = false
            )
        }
    }

    fun onClearLongPress() {
        triggerHapticAndSound(heavy = true)
        viewModelScope.launch {
            historyRepository.clearAll()
        }
        _uiState.update {
            it.copy(
                expression = "",
                livePreview = "",
                lastCalculation = "",
                errorMessage = null,
                justEvaluated = false
            )
        }
        showSnackbar("Full calculation history cleared")
    }

    fun onEqualsClick() {
        triggerHapticAndSound(heavy = true)
        val current = _uiState.value
        val rawExpr = current.expression.trim()
        if (rawExpr.isEmpty()) return

        val openCount = rawExpr.count { it == '(' }
        val closeCount = rawExpr.count { it == ')' }
        val balancedExpr = if (openCount > closeCount) {
            rawExpr + ")".repeat(openCount - closeCount)
        } else {
            rawExpr
        }

        val currentSettings = settings.value
        when (val result = MathEngine.evaluate(
            expression = balancedExpr,
            isDegreeMode = currentSettings.isDegreeMode,
            decimalPlaces = currentSettings.decimalPlaces
        )) {
            is EvalResult.Success -> {
                val historyLine = "$balancedExpr = ${result.formatted}"
                viewModelScope.launch {
                    historyRepository.addEntry(balancedExpr, result.formatted)
                }
                _uiState.update {
                    it.copy(
                        expression = result.formatted,
                        livePreview = "",
                        lastCalculation = historyLine,
                        errorMessage = null,
                        justEvaluated = true
                    )
                }
            }
            is EvalResult.Error -> {
                _uiState.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun onCopyResultClick() {
        triggerHapticAndSound()
        val state = _uiState.value
        val textToCopy = when {
            state.livePreview.isNotEmpty() -> state.livePreview
            state.expression.isNotEmpty() -> state.expression
            else -> ""
        }
        if (textToCopy.isEmpty()) {
            showSnackbar("Nothing to copy yet")
            return
        }
        feedbackManager.copyToClipboard("LK Calculator Result", textToCopy)
        showSnackbar("Copied: $textToCopy")
    }

    fun reuseHistoryEntry(entry: HistoryEntity) {
        triggerHapticAndSound()
        _uiState.update { state ->
            updateExpressionState(
                state.copy(
                    currentDestination = AppDestination.CALCULATOR,
                    lastCalculation = "${entry.expression} = ${entry.result}"
                ),
                nextExpr = entry.result,
                justEvaluated = true
            )
        }
    }

    fun deleteHistoryEntry(id: Long) {
        triggerHapticAndSound()
        viewModelScope.launch {
            historyRepository.deleteEntry(id)
        }
    }

    fun clearAllHistory() {
        triggerHapticAndSound(heavy = true)
        viewModelScope.launch {
            historyRepository.clearAll()
        }
        _uiState.update { it.copy(lastCalculation = "") }
        showSnackbar("History cleared")
    }

    private fun updateExpressionState(
        state: CalculatorUiState,
        nextExpr: String,
        justEvaluated: Boolean
    ): CalculatorUiState {
        val currentSettings = settings.value
        val preview = if (nextExpr.isNotEmpty() && !justEvaluated) {
            MathEngine.evaluatePreview(
                expression = nextExpr,
                isDegreeMode = currentSettings.isDegreeMode,
                decimalPlaces = currentSettings.decimalPlaces
            ) ?: ""
        } else {
            ""
        }
        return state.copy(
            expression = nextExpr,
            livePreview = preview,
            errorMessage = null,
            justEvaluated = justEvaluated
        )
    }

    // ---- CONVERTER ACTIONS ----

    fun selectConverterCategory(category: ConverterCategory) {
        triggerHapticAndSound()
        val units = UnitCatalog.unitsFor(category)
        val defaultFrom = units.first().code
        val defaultTo = units.getOrElse(1) { units.first() }.code
        _uiState.update { state ->
            val output = computeConversion(category, state.converterInput, defaultFrom, defaultTo, settings.value)
            state.copy(
                converterCategory = category,
                converterFromUnit = defaultFrom,
                converterToUnit = defaultTo,
                converterOutput = output
            )
        }
    }

    fun updateConverterInput(input: String) {
        _uiState.update { state ->
            val output = computeConversion(
                state.converterCategory,
                input,
                state.converterFromUnit,
                state.converterToUnit,
                settings.value
            )
            state.copy(converterInput = input, converterOutput = output)
        }
    }

    fun setConverterFromUnit(code: String) {
        triggerHapticAndSound()
        _uiState.update { state ->
            val output = computeConversion(
                state.converterCategory,
                state.converterInput,
                code,
                state.converterToUnit,
                settings.value
            )
            state.copy(converterFromUnit = code, converterOutput = output)
        }
    }

    fun setConverterToUnit(code: String) {
        triggerHapticAndSound()
        _uiState.update { state ->
            val output = computeConversion(
                state.converterCategory,
                state.converterInput,
                state.converterFromUnit,
                code,
                settings.value
            )
            state.copy(converterToUnit = code, converterOutput = output)
        }
    }

    fun swapConverterUnits() {
        triggerHapticAndSound()
        _uiState.update { state ->
            val newFrom = state.converterToUnit
            val newTo = state.converterFromUnit
            val output = computeConversion(
                state.converterCategory,
                state.converterInput,
                newFrom,
                newTo,
                settings.value
            )
            state.copy(
                converterFromUnit = newFrom,
                converterToUnit = newTo,
                converterOutput = output
            )
        }
    }

    fun refreshCurrencyRates() {
        triggerHapticAndSound()
        if (_uiState.value.isRefreshingRates) return
        _uiState.update { it.copy(isRefreshingRates = true) }
        viewModelScope.launch {
            val result = currencyRepository.refreshRates()
            _uiState.update { it.copy(isRefreshingRates = false) }
            result.onSuccess { label ->
                showSnackbar("Exchange rates updated ($label)")
            }.onFailure {
                showSnackbar("Offline mode: using saved currency rates")
            }
        }
    }

    private fun computeConversion(
        category: ConverterCategory,
        input: String,
        fromCode: String,
        toCode: String,
        currentSettings: UserSettings
    ): String {
        val value = input.toDoubleOrNull() ?: return "—"
        val converted = CalculatorsEngine.convertUnit(
            category = category,
            value = value,
            fromCode = fromCode,
            toCode = toCode,
            currencyRatesFromUsd = currentSettings.currencyRates
        )
        return MathEngine.formatNumber(converted, currentSettings.decimalPlaces.coerceAtLeast(2))
    }

    // ---- AGE CALCULATOR ACTIONS ----

    fun updateBirthDate(year: Int, monthZeroIndexed: Int, day: Int) {
        triggerHapticAndSound()
        _uiState.update {
            it.copy(
                birthYear = year,
                birthMonthZeroIndexed = monthZeroIndexed,
                birthDay = day
            )
        }
        recalculateAge()
    }

    fun updateBirthDateFromPickerMillis(utcMillis: Long) {
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        updateBirthDate(
            year = cal.get(Calendar.YEAR),
            monthZeroIndexed = cal.get(Calendar.MONTH),
            day = cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun recalculateAge() {
        _uiState.update { state ->
            val result = CalculatorsEngine.calculateAge(
                birthYear = state.birthYear,
                birthMonthZeroIndexed = state.birthMonthZeroIndexed,
                birthDay = state.birthDay
            )
            state.copy(ageResult = result)
        }
    }

    // ---- BMI CALCULATOR ACTIONS ----

    fun toggleBmiUnitSystem(isMetric: Boolean) {
        triggerHapticAndSound()
        _uiState.update { it.copy(bmiIsMetric = isMetric) }
        recalculateBmi()
    }

    fun updateBmiWeight(weight: String) {
        _uiState.update { it.copy(bmiWeightInput = weight) }
        recalculateBmi()
    }

    fun updateBmiHeightCm(heightCm: String) {
        _uiState.update { it.copy(bmiHeightCmInput = heightCm) }
        recalculateBmi()
    }

    fun updateBmiHeightImperial(feet: String, inches: String) {
        _uiState.update { it.copy(bmiHeightFtInput = feet, bmiHeightInInput = inches) }
        recalculateBmi()
    }

    private fun recalculateBmi() {
        _uiState.update { state ->
            val weightRaw = state.bmiWeightInput.toDoubleOrNull()
            val weightKg = if (weightRaw != null) {
                if (state.bmiIsMetric) weightRaw else weightRaw * 0.45359237
            } else null

            val heightCm = if (state.bmiIsMetric) {
                state.bmiHeightCmInput.toDoubleOrNull()
            } else {
                val ft = state.bmiHeightFtInput.toDoubleOrNull() ?: 0.0
                val inch = state.bmiHeightInInput.toDoubleOrNull() ?: 0.0
                val totalInches = ft * 12.0 + inch
                if (totalInches > 0) totalInches * 2.54 else null
            }

            val result = if (weightKg != null && heightCm != null) {
                CalculatorsEngine.calculateBmi(weightKg, heightCm)
            } else null

            state.copy(bmiResult = result)
        }
    }

    // ---- DISCOUNT CALCULATOR ACTIONS ----

    fun updateDiscountPrice(price: String) {
        _uiState.update { it.copy(discountPriceInput = price) }
        recalculateDiscount()
    }

    fun updateDiscountPercent(percent: String) {
        _uiState.update { it.copy(discountPercentInput = percent) }
        recalculateDiscount()
    }

    fun updateDiscountTax(tax: String) {
        _uiState.update { it.copy(discountTaxInput = tax) }
        recalculateDiscount()
    }

    private fun recalculateDiscount() {
        _uiState.update { state ->
            val price = state.discountPriceInput.toDoubleOrNull()
            val discount = state.discountPercentInput.toDoubleOrNull()
            val tax = state.discountTaxInput.toDoubleOrNull() ?: 0.0
            val result = if (price != null && discount != null) {
                CalculatorsEngine.calculateDiscount(price, discount, tax)
            } else null
            state.copy(discountResult = result)
        }
    }
}
