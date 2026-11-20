package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorUiState
import com.example.ui.components.SubScreenContainer
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    state: CalculatorUiState,
    onDateSelectedMillis: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current
    var showDatePicker by remember { mutableStateOf(false) }

    val dobCalendar = remember(state.birthYear, state.birthMonthZeroIndexed, state.birthDay) {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(state.birthYear, state.birthMonthZeroIndexed, state.birthDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val dobFormatted = remember(dobCalendar.timeInMillis) {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(dobCalendar.time)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dobCalendar.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelectedMillis(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("CONFIRM", color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    SubScreenContainer(
        title = "Age Calc",
        subtitle = "EXACT YEARS • MONTHS • DAYS",
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date of Birth Selection Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(lkColors.displayCard)
                    .border(1.5.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DATE OF BIRTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed
                )

                Text(
                    text = dobFormatted,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("age_dob_text")
                )

                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonRed,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("select_dob_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = "Select Date of Birth",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "PICK DATE OF BIRTH",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Age Result Cards
            val age = state.ageResult
            if (age == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(lkColors.keyOperatorBackground)
                        .border(1.dp, NeonRed, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Date of Birth cannot be in the future.",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonRed
                    )
                }
            } else {
                Text(
                    text = "YOUR EXACT AGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = lkColors.historyText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AgeStatTile(
                        value = age.years.toString(),
                        label = "YEARS",
                        highlighted = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("age_years_card")
                    )
                    AgeStatTile(
                        value = age.months.toString(),
                        label = "MONTHS",
                        highlighted = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("age_months_card")
                    )
                    AgeStatTile(
                        value = age.days.toString(),
                        label = "DAYS",
                        highlighted = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("age_days_card")
                    )
                }

                // Detailed Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(lkColors.displayCard)
                        .border(1.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "LIFE TIMELINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRed
                    )
                    SummaryRow("Total Days Lived", "${age.totalDays} days")
                    SummaryRow("Total Weeks Lived", "${age.totalWeeks} weeks")
                    SummaryRow(
                        "Next Birthday In",
                        if (age.daysUntilNextBirthday == 0) "Today! Happy Birthday!" else "${age.daysUntilNextBirthday} days"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AgeStatTile(
    value: String,
    label: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (highlighted) NeonRed else lkColors.displayCard)
            .border(
                1.5.dp,
                if (highlighted) NeonRed else lkColors.keyBorder,
                RoundedCornerShape(18.dp)
            )
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp
            ),
            color = if (highlighted) PureWhite else MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlighted) PureWhite.copy(alpha = 0.9f) else lkColors.historyText
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalLkColors.current.historyText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
