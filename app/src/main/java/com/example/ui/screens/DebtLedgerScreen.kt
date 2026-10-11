package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.data.model.DebtEntity
import com.example.localization.AppStrings
import com.example.ui.theme.DebtOrange
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtLedgerScreen(
    strings: AppStrings,
    debts: List<DebtEntity>,
    totalReceivable: Double,
    totalPayable: Double,
    primaryCurrency: CurrencyCode,
    privacyMode: Boolean,
    onAddDebt: (contactName: String, phone: String?, type: String, amount: Double, currency: String, dueDate: Long?, notes: String?) -> Unit,
    onToggleSettled: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var filterTab by remember { mutableStateOf("ALL") } // ALL, PENDING, SETTLED

    val filteredDebts = remember(debts, filterTab) {
        when (filterTab) {
            "PENDING" -> debts.filter { !it.isSettled }
            "SETTLED" -> debts.filter { it.isSettled }
            else -> debts
        }
    }

    val formattedReceivable = remember(totalReceivable, privacyMode) {
        if (privacyMode) "••••••" else String.format(Locale.US, "%,.2f", totalReceivable)
    }
    val formattedPayable = remember(totalPayable, privacyMode) {
        if (privacyMode) "••••••" else String.format(Locale.US, "%,.2f", totalPayable)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("debt_ledger_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Receivable & Payable Summary Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Receivable (Qaba / You'll Get)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.toReceive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IncomeGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${primaryCurrency.symbol} $formattedReceivable",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Payable (Narra jira / You'll Give)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.toPay,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${primaryCurrency.symbol} $formattedPayable",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Action Toolbar & Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterTab == "ALL",
                        onClick = { filterTab = "ALL" },
                        label = { Text(strings.allTime, fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = filterTab == "PENDING",
                        onClick = { filterTab = "PENDING" },
                        label = { Text(strings.pending, fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = filterTab == "SETTLED",
                        onClick = { filterTab = "SETTLED" },
                        label = { Text(strings.settled, fontSize = 12.sp) }
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("add_debt_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addDebt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Debt list
        if (filteredDebts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.noDebtRecords,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredDebts, key = { it.id }) { debt ->
                val isReceivable = debt.type == "TO_RECEIVE"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_item_${debt.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (debt.isSettled) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (debt.isSettled) 0.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (debt.isSettled) Color.Gray.copy(alpha = 0.2f)
                                    else if (isReceivable) IncomeGreen.copy(alpha = 0.15f)
                                    else ExpenseRed.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (debt.isSettled) Icons.Default.CheckCircle else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (debt.isSettled) Color.Gray else if (isReceivable) IncomeGreen else ExpenseRed
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = debt.contactName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isReceivable) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (isReceivable) strings.toReceive else strings.toPay,
                                        color = if (isReceivable) IncomeGreen else ExpenseRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!debt.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = debt.notes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!debt.phoneNumber.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${debt.phoneNumber}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = debt.phoneNumber,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${primaryCurrency.symbol} ${if (privacyMode) "••••" else String.format(Locale.US, "%,.2f", debt.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (debt.isSettled) Color.Gray else if (isReceivable) IncomeGreen else ExpenseRed
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (debt.isSettled) MaterialTheme.colorScheme.surfaceVariant else EmeraldPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onToggleSettled(debt) }
                                ) {
                                    Text(
                                        text = if (debt.isSettled) strings.settled else strings.markSettled,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteDebt(debt) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = strings.delete,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
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

    // Add Debt Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var debtType by remember { mutableStateOf("TO_RECEIVE") }
        var amountText by remember { mutableStateOf("") }
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.addDebt, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Type selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { debtType = "TO_RECEIVE" },
                            color = if (debtType == "TO_RECEIVE") IncomeGreen else Color.Transparent
                        ) {
                            Text(
                                text = strings.toReceive,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (debtType == "TO_RECEIVE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { debtType = "TO_PAY" },
                            color = if (debtType == "TO_PAY") ExpenseRed else Color.Transparent
                        ) {
                            Text(
                                text = strings.toPay,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (debtType == "TO_PAY") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.contactName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Bilbila / Phone") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                        label = { Text(strings.amount) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(strings.notes) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = amountText.toDoubleOrNull()
                        if (name.isNotBlank() && parsed != null && parsed > 0.0) {
                            onAddDebt(
                                name,
                                phone,
                                debtType,
                                parsed,
                                primaryCurrency.code,
                                null,
                                noteText
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(strings.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
