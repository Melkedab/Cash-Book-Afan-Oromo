package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.theme.CbePurple
import com.example.ui.theme.ExpenseContainerLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeContainerLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TelebirrCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    strings: AppStrings,
    language: AppLanguage,
    primaryCurrency: CurrencyCode,
    privacyMode: Boolean,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isIncome = transaction.type == "INCOME"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(transaction.timestamp) { timeFormat.format(Date(transaction.timestamp)) }

    val category = remember(transaction.category, transaction.type) {
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        TransactionCategory.findCategory(transaction.category, type)
    }

    val displayAmount = remember(transaction.amount, transaction.exchangeRateToPrimary, privacyMode) {
        if (privacyMode) "••••••" else String.format(Locale.US, "%,.2f", transaction.amountInPrimary)
    }

    val paymentModeLabel = when (transaction.paymentMode) {
        "TELEBIRR" -> strings.telebirr
        "CBE_BIRR" -> strings.cbeBirr
        "BANK" -> strings.bankTransfer
        "CARD" -> strings.cardPayment
        "CASH" -> strings.cashInHand
        else -> strings.otherPayment
    }

    val paymentBadgeColor = when (transaction.paymentMode) {
        "TELEBIRR" -> TelebirrCyan
        "CBE_BIRR" -> CbePurple
        "BANK" -> Color(0xFF1E5245)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category / Direction Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) IncomeContainerLight else ExpenseContainerLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.getDisplayName(language),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Contact person or Notes
                if (!transaction.contactName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaction.contactName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else if (!transaction.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Badges row: Payment Mode, Reference, Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Payment Mode Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = paymentBadgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = paymentModeLabel,
                            color = paymentBadgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (!transaction.referenceNumber.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#${transaction.referenceNumber}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount Column
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ${primaryCurrency.symbol} $displayAmount",
                    color = if (isIncome) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // If logged in a foreign currency like USD, show original amount
                if (transaction.currency != primaryCurrency.code) {
                    val currObj = CurrencyCode.fromCode(transaction.currency)
                    Text(
                        text = "(${currObj.symbol} ${String.format(Locale.US, "%,.2f", transaction.amount)})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More Options dropdown
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(strings.edit) },
                        onClick = {
                            menuExpanded = false
                            onEdit(transaction)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.delete, color = ExpenseRed) },
                        onClick = {
                            menuExpanded = false
                            onDelete(transaction)
                        }
                    )
                }
            }
        }
    }
}
