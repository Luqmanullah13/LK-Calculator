package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.AppDestination
import com.example.ui.CalculatorUiState
import com.example.ui.CalculatorViewModel
import com.example.ui.components.LkBrandingHeader
import com.example.ui.theme.LocalLkColors
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureWhite

private data class FeatureChipItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

private val featureChips = listOf(
    FeatureChipItem(AppDestination.HISTORY, "History", Icons.Rounded.History, "nav_history"),
    FeatureChipItem(AppDestination.CONVERTER, "Converter", Icons.Rounded.CurrencyExchange, "nav_converter"),
    FeatureChipItem(AppDestination.AGE, "Age Calc", Icons.Rounded.Cake, "nav_age"),
    FeatureChipItem(AppDestination.BMI, "BMI", Icons.Rounded.MonitorWeight, "nav_bmi"),
    FeatureChipItem(AppDestination.DISCOUNT, "Discount", Icons.Rounded.LocalOffer, "nav_discount")
)

private enum class KeyStyle {
    NUMBER,
    OPERATOR_RED,
    ACCENT_SOLID_RED,
    FUNCTION_OUTLINE,
    SCIENTIFIC
}

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    settings: UserSettings,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 600.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LkBrandingHeader(
            isDarkTheme = settings.isDarkTheme,
            isSoundEnabled = settings.soundEnabled,
            onToggleTheme = viewModel::toggleDarkTheme,
            onToggleSound = viewModel::toggleSound,
            onOpenSettings = { viewModel.navigateTo(AppDestination.SETTINGS) }
        )

        // All-in-One Feature Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(featureChips, key = { it.destination }) { chip ->
                Surface(
                    onClick = { viewModel.navigateTo(chip.destination) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(36.dp)
                        .border(1.dp, lkColors.keyBorder, RoundedCornerShape(20.dp))
                        .testTag(chip.tag)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = chip.icon,
                            contentDescription = chip.label,
                            tint = NeonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Scientific ON/OFF + DEG/RAD + Copy Result Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = viewModel::toggleScientificMode,
                    shape = RoundedCornerShape(18.dp),
                    color = if (state.isScientificMode) NeonRed else lkColors.displayCard,
                    modifier = Modifier
                        .height(34.dp)
                        .border(
                            width = 1.dp,
                            color = if (state.isScientificMode) NeonRed else lkColors.keyOperatorBorder,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("scientific_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Science,
                            contentDescription = "Scientific Mode",
                            tint = if (state.isScientificMode) PureWhite else NeonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (state.isScientificMode) "SCIENTIFIC ON" else "SCIENTIFIC OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (state.isScientificMode) PureWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                AnimatedVisibility(visible = state.isScientificMode) {
                    Surface(
                        onClick = viewModel::toggleDegreeMode,
                        shape = RoundedCornerShape(18.dp),
                        color = lkColors.displayCard,
                        modifier = Modifier
                            .height(34.dp)
                            .border(1.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                            .testTag("deg_rad_toggle_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (settings.isDegreeMode) "DEG" else "RAD",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = NeonRed
                            )
                        }
                    }
                }
            }

            // Copy Result Button
            Surface(
                onClick = viewModel::onCopyResultClick,
                shape = RoundedCornerShape(18.dp),
                color = lkColors.displayCard,
                modifier = Modifier
                    .height(34.dp)
                    .border(1.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
                    .testTag("copy_result_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy Result",
                        tint = NeonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Copy Result",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Main & Secondary Calculation Display
        CalculatorDisplayPanel(
            expression = state.expression,
            livePreview = state.livePreview,
            lastCalculation = state.lastCalculation,
            errorMessage = state.errorMessage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Collapsible Scientific Pad
        AnimatedVisibility(
            visible = state.isScientificMode,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            ScientificKeypad(
                onSciClick = viewModel::onScientificFunctionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }

        // Main 4-Column Round Keypad Grid (fills remaining vertical space cleanly)
        MainCalculatorKeypad(
            isScientific = state.isScientificMode,
            onDigit = viewModel::onDigitClick,
            onDecimal = viewModel::onDecimalClick,
            onOperator = viewModel::onOperatorClick,
            onPercent = viewModel::onPercentClick,
            onParenthesis = viewModel::onParenthesisClick,
            onBackspace = viewModel::onBackspaceClick,
            onClear = viewModel::onClearClick,
            onClearLongPress = viewModel::onClearLongPress,
            onEquals = viewModel::onEqualsClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CalculatorDisplayPanel(
    expression: String,
    livePreview: String,
    lastCalculation: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current
    val exprScroll = rememberScrollState()

    LaunchedEffect(expression) {
        exprScroll.animateScrollTo(exprScroll.maxValue)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(lkColors.displayCard)
            .border(1.5.dp, lkColors.keyBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Second Display: Small Grey - Shows History of last calculation
        Text(
            text = if (lastCalculation.isNotEmpty()) lastCalculation else "READY // AC HOLD CLEARS HISTORY",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontFamily = OrbitronFontFamily
            ),
            color = lkColors.historyText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("display_last_history")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Top Display: Big White Numbers showing expression live
        val displayExpr = expression.ifEmpty { "0" }
        val exprFontSize = when {
            displayExpr.length > 16 -> 22.sp
            displayExpr.length > 11 -> 28.sp
            else -> 34.sp
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(exprScroll),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = displayExpr,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = exprFontSize,
                    lineHeight = (exprFontSize.value + 6).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier = Modifier.testTag("display_expression")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Live calculation preview (e.g. "= 17") or error message
        val subText = when {
            errorMessage != null -> errorMessage
            livePreview.isNotEmpty() -> "= $livePreview"
            else -> ""
        }
        Text(
            text = subText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (errorMessage != null) NeonRed else lkColors.historyText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier
                .height(26.dp)
                .testTag("display_live_preview")
        )
    }
}

@Composable
private fun ScientificKeypad(
    onSciClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val row1 = listOf("sin", "cos", "tan", "√", "^", "!")
    val row2 = listOf("π", "log", "ln", "e", "1/x")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            row1.forEach { symbol ->
                SciKeyButton(
                    label = symbol,
                    onClick = { onSciClick(symbol) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sci_key_$symbol")
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            row2.forEach { symbol ->
                SciKeyButton(
                    label = symbol,
                    onClick = { onSciClick(symbol) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sci_key_$symbol")
                )
            }
        }
    }
}

@Composable
private fun SciKeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lkColors = LocalLkColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "sci_press"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(14.dp),
        color = lkColors.keySciBackground,
        modifier = modifier
            .scale(scale)
            .height(38.dp)
            .border(1.dp, lkColors.keyBorder, RoundedCornerShape(14.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MainCalculatorKeypad(
    isScientific: Boolean,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onPercent: () -> Unit,
    onParenthesis: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClearLongPress: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val rowGap = if (isScientific) 6.dp else 10.dp
        val colGap = 10.dp
        val rowHeight = ((maxHeight - rowGap * 4) / 5).coerceIn(48.dp, 76.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(rowGap, Alignment.Bottom)
        ) {
            // Row 1: AC (Red), ( ) , % , ÷ (Red)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(colGap)
            ) {
                CalcRoundButton(
                    label = "AC",
                    keyStyle = KeyStyle.OPERATOR_RED,
                    height = rowHeight,
                    onClick = onClear,
                    onLongClick = onClearLongPress,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_ac")
                )
                CalcRoundButton(
                    label = "( )",
                    keyStyle = KeyStyle.FUNCTION_OUTLINE,
                    height = rowHeight,
                    onClick = onParenthesis,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_parens")
                )
                CalcRoundButton(
                    label = "%",
                    keyStyle = KeyStyle.FUNCTION_OUTLINE,
                    height = rowHeight,
                    onClick = onPercent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_percent")
                )
                CalcRoundButton(
                    label = "÷",
                    keyStyle = KeyStyle.OPERATOR_RED,
                    height = rowHeight,
                    onClick = { onOperator("÷") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_divide")
                )
            }

            // Row 2: 7, 8, 9, × (Red)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(colGap)
            ) {
                listOf("7", "8", "9").forEach { d ->
                    CalcRoundButton(
                        label = d,
                        keyStyle = KeyStyle.NUMBER,
                        height = rowHeight,
                        onClick = { onDigit(d) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_$d")
                    )
                }
                CalcRoundButton(
                    label = "×",
                    keyStyle = KeyStyle.OPERATOR_RED,
                    height = rowHeight,
                    onClick = { onOperator("×") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_multiply")
                )
            }

            // Row 3: 4, 5, 6, - (Red)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(colGap)
            ) {
                listOf("4", "5", "6").forEach { d ->
                    CalcRoundButton(
                        label = d,
                        keyStyle = KeyStyle.NUMBER,
                        height = rowHeight,
                        onClick = { onDigit(d) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_$d")
                    )
                }
                CalcRoundButton(
                    label = "-",
                    keyStyle = KeyStyle.OPERATOR_RED,
                    height = rowHeight,
                    onClick = { onOperator("-") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_minus")
                )
            }

            // Row 4: 1, 2, 3, + (Red)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(colGap)
            ) {
                listOf("1", "2", "3").forEach { d ->
                    CalcRoundButton(
                        label = d,
                        keyStyle = KeyStyle.NUMBER,
                        height = rowHeight,
                        onClick = { onDigit(d) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("key_$d")
                    )
                }
                CalcRoundButton(
                    label = "+",
                    keyStyle = KeyStyle.OPERATOR_RED,
                    height = rowHeight,
                    onClick = { onOperator("+") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("key_plus")
                )
            }

            // Row 5: 0 (Big Double), . , ⌫ (Delete), = (Solid RED Big)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(colGap)
            ) {
                CalcRoundButton(
                    label = "0",
                    keyStyle = KeyStyle.NUMBER,
                    height = rowHeight,
                    onClick = { onDigit("0") },
                    modifier = Modifier
                        .weight(1.65f)
                        .testTag("key_0")
                )
                CalcRoundButton(
                    label = ".",
                    keyStyle = KeyStyle.NUMBER,
                    height = rowHeight,
                    onClick = onDecimal,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("key_dot")
                )
                CalcRoundButton(
                    label = "⌫",
                    icon = Icons.AutoMirrored.Rounded.Backspace,
                    keyStyle = KeyStyle.FUNCTION_OUTLINE,
                    height = rowHeight,
                    onClick = onBackspace,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("key_backspace")
                )
                CalcRoundButton(
                    label = "=",
                    keyStyle = KeyStyle.ACCENT_SOLID_RED,
                    height = rowHeight,
                    onClick = onEquals,
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("key_equals")
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalcRoundButton(
    label: String,
    keyStyle: KeyStyle,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onLongClick: (() -> Unit)? = null
) {
    val lkColors = LocalLkColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "calc_key_scale"
    )

    val containerColor: Color
    val borderColor: Color
    val contentColor: Color

    when (keyStyle) {
        KeyStyle.NUMBER -> {
            containerColor = lkColors.keyBackground
            borderColor = lkColors.keyBorder
            contentColor = MaterialTheme.colorScheme.onBackground
        }
        KeyStyle.OPERATOR_RED -> {
            containerColor = lkColors.keyOperatorBackground
            borderColor = lkColors.keyOperatorBorder
            contentColor = NeonRed
        }
        KeyStyle.ACCENT_SOLID_RED -> {
            containerColor = NeonRed
            borderColor = NeonRed
            contentColor = PureWhite
        }
        KeyStyle.FUNCTION_OUTLINE -> {
            containerColor = lkColors.keySciBackground
            borderColor = lkColors.keyBorder
            contentColor = MaterialTheme.colorScheme.onBackground
        }
        KeyStyle.SCIENTIFIC -> {
            containerColor = lkColors.keySciBackground
            borderColor = lkColors.keyBorder
            contentColor = MaterialTheme.colorScheme.onBackground
        }
    }

    val fontSize = when {
        label == "=" -> 28.sp
        label == "AC" || label == "( )" -> 18.sp
        else -> 22.sp
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = NeonRed.copy(alpha = 0.35f)),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                ),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
