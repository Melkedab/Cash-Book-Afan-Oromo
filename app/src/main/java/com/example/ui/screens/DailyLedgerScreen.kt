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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashBookEntity
import com.example.data.model.CurrencyCode
import com.example.data.model.TransactionEntity
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.ui.DailyLedgerGroup
import com.example.ui.DateFilterOption
import com.example.ui.TypeFilterOption
import com.example.ui.components.BalanceCard
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun DailyLedgerScreen(
    strings: AppStrings,
    language: AppLanguage,
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    primaryCurrency: CurrencyCode,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    activeBook: CashBookEntity?,
    allBooks: List<CashBookEntity>,
    onSelectBook: (Long) -> Unit,
    onCreateNewBook: () -> Unit,
    dailyGroups: List<DailyLedgerGroup>,
    dateFilter: DateFilterOption,
    onDateFilterChange: (DateFilterOption) -> Unit,
    typeFilter: TypeFilterOption,
    onTypeFilterChange: (TypeFilterOption) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_ledger_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Balance Card
        item {
            BalanceCard(
                strings = strings,
                netBalance = netBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                primaryCurrency = primaryCurrency,
                privacyMode = privacyMode,
                onTogglePrivacy = onTogglePrivacy,
                activeBook = activeBook,
                allBooks = allBooks,
                onSelectBook = onSelectBook,
                onCreateNewBook = onCreateNewBook,
                onAddIncome = onAddIncome,
                onAddExpense = onAddExpense
            )
        }

        // Search & Filter Toolbar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text(strings.searchPlaceholder, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = strings.search,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )

                // Date Filter Pills (Har'a, Kaleessa, Torban kana, Ji'a kana, Hunda)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dateOptions = listOf(
                        DateFilterOption.ALL to strings.allTime,
                        DateFilterOption.TODAY to strings.today,
                        DateFilterOption.YESTERDAY to strings.yesterday,
                        DateFilterOption.THIS_WEEK to strings.thisWeek,
                        DateFilterOption.THIS_MONTH to strings.thisMonth
                    )
                    dateOptions.forEach { (opt, label) ->
                        val isSelected = dateFilter == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDateFilterChange(opt) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Daily Ledger List
        if (dailyGroups.isEmpty()) {
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
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.noTransactionsFound,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.addFirstTransaction,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            dailyGroups.forEach { group ->
                // Daily Section Header Card
                item(key = "header_${group.dateKey}") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.dateDisplay,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Day Totals (In & Out)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (group.cashInTotal > 0.0) {
                                    Text(
                                        text = "+${primaryCurrency.symbol}${if (privacyMode) "••" else String.format(Locale.US, "%,.0f", group.cashInTotal)}",
                                        color = IncomeGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (group.cashOutTotal > 0.0) {
                                    Text(
                                        text = "-${primaryCurrency.symbol}${if (privacyMode) "••" else String.format(Locale.US, "%,.0f", group.cashOutTotal)}",
                                        color = ExpenseRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Transactions within this day
                items(group.transactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        strings = strings,
                        language = language,
                        primaryCurrency = primaryCurrency,
                        privacyMode = privacyMode,
                        onEdit = onEditTransaction,
                        onDelete = onDeleteTransaction
                    )
                }
            }
        }

        // Bottom spacing for navigation bar
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
