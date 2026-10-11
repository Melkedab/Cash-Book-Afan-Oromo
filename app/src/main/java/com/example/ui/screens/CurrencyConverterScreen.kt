package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary
import java.util.Locale

@Composable
fun CurrencyConverterScreen(
    strings: AppStrings,
    language: AppLanguage,
    exchangeRates: Map<String, Double>,
    onUpdateRate: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf("ETB") }
    var amountInput by remember { mutableStateOf("100") }

    var editingCurrencyCode by remember { mutableStateOf<String?>(null) }
    var newRateText by remember { mutableStateOf("") }

    val fromRate = exchangeRates[fromCode] ?: 1.0
    val toRate = exchangeRates[toCode] ?: 1.0

    val inputVal = amountInput.toDoubleOrNull() ?: 0.0
    // Conversion: amount * (fromRate in ETB) / (toRate in ETB)
    val convertedVal = (inputVal * fromRate) / toRate

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("currency_converter_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Converter Calculator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = EmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.converterTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // From & To selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // From Currency
                        CurrencyPicker(
                            selectedCode = fromCode,
                            onSelect = { fromCode = it },
                            modifier = Modifier.weight(1f)
                        )

                        // Swap Button
                        IconButton(
                            onClick = {
                                val temp = fromCode
                                fromCode = toCode
                                toCode = temp
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "Swap",
                                tint = EmeraldPrimary
                            )
                        }

                        // To Currency
                        CurrencyPicker(
                            selectedCode = toCode,
                            onSelect = { toCode = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Input
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountInput = it },
                        label = { Text(strings.amount) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_amount_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Converted Output Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = strings.convertedAmount,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val toCurr = CurrencyCode.fromCode(toCode)
                            Text(
                                text = "${toCurr.symbol} ${String.format(Locale.US, "%,.2f", convertedVal)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1 $fromCode = ${String.format(Locale.US, "%.4f", fromRate / toRate)} $toCode",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Custom Exchange Rates Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.customRates,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1 [Unit] = X ETB (Birr)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    CurrencyCode.entries.filter { it != CurrencyCode.ETB }.forEach { curr ->
                        val currentRate = exchangeRates[curr.code] ?: curr.defaultRateToETB
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = curr.getDisplayName(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "1 ${curr.code} = ${String.format(Locale.US, "%,.2f", currentRate)} ETB",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        editingCurrencyCode = curr.code
                                        newRateText = currentRate.toString()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.edit,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    // Rate editing dialog
    if (editingCurrencyCode != null) {
        AlertDialog(
            onDismissRequest = { editingCurrencyCode = null },
            title = { Text("${strings.updateRate}: $editingCurrencyCode", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newRateText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) newRateText = it },
                    label = { Text("Gatii ETB / Rate in ETB") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newRateText.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onUpdateRate(editingCurrencyCode!!, parsed)
                        }
                        editingCurrencyCode = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingCurrencyCode = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun CurrencyPicker(
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val curr = CurrencyCode.fromCode(selectedCode)
                Text(
                    text = "${curr.code} (${curr.symbol})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CurrencyCode.entries.forEach { c ->
                DropdownMenuItem(
                    text = { Text("${c.code} (${c.symbol}) - ${c.nameEn}") },
                    onClick = {
                        onSelect(c.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
