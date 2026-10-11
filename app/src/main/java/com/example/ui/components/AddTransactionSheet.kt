package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.theme.ExpenseContainerLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeContainerLight
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    initialType: String = "INCOME",
    existingTransaction: TransactionEntity? = null,
    strings: AppStrings,
    language: AppLanguage,
    primaryCurrency: CurrencyCode,
    exchangeRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onSave: (
        type: String,
        amount: Double,
        currency: String,
        category: String,
        paymentMode: String,
        timestamp: Long,
        contactName: String?,
        notes: String?,
        referenceNumber: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(existingTransaction?.type ?: initialType) }
    var amountText by remember {
        mutableStateOf(existingTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var selectedCurrency by remember {
        mutableStateOf(existingTransaction?.currency ?: primaryCurrency.code)
    }

    val availableCategories = remember(type) {
        if (type == "INCOME") TransactionCategory.DefaultIncomeCategories
        else TransactionCategory.DefaultExpenseCategories
    }

    var selectedCategoryId by remember(type) {
        mutableStateOf(
            existingTransaction?.category ?: availableCategories.first().id
        )
    }

    var selectedPaymentMode by remember {
        mutableStateOf(existingTransaction?.paymentMode ?: "CASH")
    }

    var contactName by remember { mutableStateOf(existingTransaction?.contactName ?: "") }
    var notes by remember { mutableStateOf(existingTransaction?.notes ?: "") }
    var referenceNumber by remember { mutableStateOf(existingTransaction?.referenceNumber ?: "") }
    var timestamp by remember { mutableLongStateOf(existingTransaction?.timestamp ?: System.currentTimeMillis()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickAmounts = listOf(100.0, 500.0, 1000.0, 5000.0, 10000.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction != null) strings.editTransaction else strings.addTransaction,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = strings.cancel)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Income / Expense Segmented Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Income Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            type = "INCOME"
                            selectedCategoryId = TransactionCategory.DefaultIncomeCategories.first().id
                        }
                        .testTag("type_income_toggle"),
                    color = if (type == "INCOME") IncomeGreen else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.cashIn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expense Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            type = "EXPENSE"
                            selectedCategoryId = TransactionCategory.DefaultExpenseCategories.first().id
                        }
                        .testTag("type_expense_toggle"),
                    color = if (type == "EXPENSE") ExpenseRed else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.cashOut,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input with Currency Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                            errorMessage = null
                        }
                    },
                    label = { Text(strings.amount) },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Currency selector chip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    var currencyMenuOpen by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clickable { currencyMenuOpen = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedCurrency,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 15.sp
                        )

                        androidx.compose.material3.DropdownMenu(
                            expanded = currencyMenuOpen,
                            onDismissRequest = { currencyMenuOpen = false }
                        ) {
                            CurrencyCode.entries.forEach { c ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("${c.code} (${c.symbol})") },
                                    onClick = {
                                        selectedCurrency = c.code
                                        currencyMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = ExpenseRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            // Quick amount chips (+100, +500, +1000, etc.)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickAmounts.forEach { amt ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val current = amountText.toDoubleOrNull() ?: 0.0
                                amountText = (current + amt).toInt().toString()
                            }
                    ) {
                        Text(
                            text = "+${amt.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = strings.category,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = cat.id },
                        label = {
                            Text(
                                text = cat.getDisplayName(language),
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == "INCOME") IncomeContainerLight else ExpenseContainerLight,
                            selectedLabelColor = if (type == "INCOME") IncomeGreen else ExpenseRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Mode Selector
            Text(
                text = strings.paymentMode,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val modes = listOf(
                    "CASH" to strings.cashInHand,
                    "TELEBIRR" to strings.telebirr,
                    "CBE_BIRR" to strings.cbeBirr,
                    "BANK" to strings.bankTransfer,
                    "CARD" to strings.cardPayment,
                    "OTHER" to strings.otherPayment
                )
                modes.forEach { (code, label) ->
                    val isSelected = selectedPaymentMode == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPaymentMode = code },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact / Party Name
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text(strings.contactName) },
                placeholder = { Text(strings.optionalContact) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes / Remarks
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notes) },
                placeholder = { Text(strings.optionalNotes) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reference / Receipt #
            OutlinedTextField(
                value = referenceNumber,
                onValueChange = { referenceNumber = it },
                label = { Text(strings.referenceNo) },
                placeholder = { Text("e.g. REC-1029 / REF-88") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (Save & Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(strings.cancel)
                }

                Button(
                    onClick = {
                        val parsed = amountText.toDoubleOrNull()
                        if (parsed == null || parsed <= 0.0) {
                            errorMessage = if (language == AppLanguage.AFAAN_OROMOO) "Mee hamma qarshii sirrii galchaa" else "Please enter a valid amount"
                        } else {
                            onSave(
                                type,
                                parsed,
                                selectedCurrency,
                                selectedCategoryId,
                                selectedPaymentMode,
                                timestamp,
                                contactName,
                                notes,
                                referenceNumber
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "INCOME") IncomeGreen else ExpenseRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_transaction_button")
                ) {
                    Text(strings.save, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
