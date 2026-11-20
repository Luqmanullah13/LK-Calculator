package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppDestination
import com.example.ui.CalculatorViewModel
import com.example.ui.screens.AgeCalculatorScreen
import com.example.ui.screens.BmiCalculatorScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.DiscountCalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LkCalculatorTheme
import com.example.ui.theme.NeonRed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        setContent {
            val viewModel: CalculatorViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            LaunchedEffect(settings.isDarkTheme) {
                if (settings.isDarkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(Color.BLACK),
                        navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.BLACK),
                        navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.BLACK)
                    )
                }
            }

            LkCalculatorTheme(darkTheme = settings.isDarkTheme) {
                LkCalculatorApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LkCalculatorApp(viewModel: CalculatorViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .border(1.dp, NeonRed, RoundedCornerShape(14.dp))
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = uiState.currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "destination_transition"
            ) { destination ->
                when (destination) {
                    AppDestination.CALCULATOR -> CalculatorScreen(
                        state = uiState,
                        settings = settings,
                        viewModel = viewModel
                    )
                    AppDestination.HISTORY -> HistoryScreen(
                        historyList = historyList,
                        onReuseEntry = viewModel::reuseHistoryEntry,
                        onDeleteEntry = viewModel::deleteHistoryEntry,
                        onClearHistory = viewModel::clearAllHistory,
                        onBack = viewModel::navigateBack
                    )
                    AppDestination.CONVERTER -> ConverterScreen(
                        state = uiState,
                        settings = settings,
                        onSelectCategory = viewModel::selectConverterCategory,
                        onInputChange = viewModel::updateConverterInput,
                        onSelectFromUnit = viewModel::setConverterFromUnit,
                        onSelectToUnit = viewModel::setConverterToUnit,
                        onSwapUnits = viewModel::swapConverterUnits,
                        onRefreshRates = viewModel::refreshCurrencyRates,
                        onBack = viewModel::navigateBack
                    )
                    AppDestination.AGE -> AgeCalculatorScreen(
                        state = uiState,
                        onDateSelectedMillis = viewModel::updateBirthDateFromPickerMillis,
                        onBack = viewModel::navigateBack
                    )
                    AppDestination.BMI -> BmiCalculatorScreen(
                        state = uiState,
                        onToggleMetric = viewModel::toggleBmiUnitSystem,
                        onWeightChange = viewModel::updateBmiWeight,
                        onHeightCmChange = viewModel::updateBmiHeightCm,
                        onHeightImperialChange = viewModel::updateBmiHeightImperial,
                        onBack = viewModel::navigateBack
                    )
                    AppDestination.DISCOUNT -> DiscountCalculatorScreen(
                        state = uiState,
                        decimalPlaces = settings.decimalPlaces,
                        onPriceChange = viewModel::updateDiscountPrice,
                        onDiscountChange = viewModel::updateDiscountPercent,
                        onTaxChange = viewModel::updateDiscountTax,
                        onBack = viewModel::navigateBack
                    )
                    AppDestination.SETTINGS -> SettingsScreen(
                        settings = settings,
                        onSelectDecimalPlaces = viewModel::setDecimalPlaces,
                        onToggleVibrate = viewModel::toggleVibrate,
                        onToggleSound = viewModel::toggleSound,
                        onToggleDarkTheme = viewModel::toggleDarkTheme,
                        onToggleDegreeMode = viewModel::toggleDegreeMode,
                        onBack = viewModel::navigateBack
                    )
                }
            }
        }
    }
}
