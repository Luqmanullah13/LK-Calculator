package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryEntity
import com.example.ui.components.SubScreenContainer
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<HistoryEntity>,
    onReuseEntry: (HistoryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current

    SubScreenContainer(
        title = "History",
        subtitle = "TAP TO REUSE CALCULATION",
        onBack = onBack,
        modifier = modifier,
        trailingAction = {
            if (historyList.isNotEmpty()) {
                Button(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonRed,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear History",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Clear History",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }
            }
        }
    ) {
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "Empty History",
                        tint = NeonRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "NO CALCULATIONS YET",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = OrbitronFontFamily),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your solved expressions will appear here.\nTap any entry to reuse its result, or hold AC to clear all.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = lkColors.historyText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(historyList, key = { it.id }) { entry ->
                    val timeStr = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())
                        .format(Date(entry.timestamp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(lkColors.displayCard)
                            .border(1.dp, lkColors.keyBorder, RoundedCornerShape(16.dp))
                            .clickable { onReuseEntry(entry) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("history_item_${entry.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = lkColors.historyText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.expression,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = OrbitronFontFamily),
                                color = lkColors.historyText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "= ${entry.result}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        IconButton(
                            onClick = { onDeleteEntry(entry.id) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .testTag("delete_history_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete calculation",
                                tint = NeonRed
                            )
                        }
                    }
                }
            }
        }
    }
}
