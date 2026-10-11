package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashBookEntity
import com.example.data.model.CurrencyCode
import com.example.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun BalanceCard(
    strings: AppStrings,
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
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bookDropdownExpanded by remember { mutableStateOf(false) }

    val formattedNet = remember(netBalance, privacyMode) {
        if (privacyMode) "••••••••" else String.format(Locale.US, "%,.2f", netBalance)
    }
    val formattedIncome = remember(totalIncome, privacyMode) {
        if (privacyMode) "••••••" else String.format(Locale.US, "%,.2f", totalIncome)
    }
    val formattedExpense = remember(totalExpense, privacyMode) {
        if (privacyMode) "••••••" else String.format(Locale.US, "%,.2f", totalExpense)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            EmeraldPrimary,
                            Color(0xFF073822)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header with Book switcher & Privacy Mode button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active Book pill
                    Box {
                        Surface(
                            onClick = { bookDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33FFFFFF),
                            modifier = Modifier.testTag("book_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = GoldTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeBook?.name ?: strings.activeBook,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = bookDropdownExpanded,
                            onDismissRequest = { bookDropdownExpanded = false }
                        ) {
                            Text(
                                text = strings.manageBooks,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            allBooks.forEach { book ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = book.name,
                                            fontWeight = if (book.id == activeBook?.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectBook(book.id)
                                        bookDropdownExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(strings.createNewBook, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    bookDropdownExpanded = false
                                    onCreateNewBook()
                                }
                            )
                        }
                    }

                    // Privacy toggle button
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("privacy_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (privacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (privacyMode) strings.showBalance else strings.hideBalance,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Net Balance Label & Value
                Text(
                    text = strings.netBalance,
                    color = Color(0xFFD4EBDC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${primaryCurrency.symbol} ",
                        color = GoldTertiary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedNet,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Income & Expense summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Cash In (Income)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color(0x24FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.cashIn,
                                    color = Color(0xFFC0E8D0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${primaryCurrency.symbol} $formattedIncome",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Total Cash Out (Expense)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color(0x24FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.cashOut,
                                    color = Color(0xFFFFD4D4),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${primaryCurrency.symbol} $formattedExpense",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Fast Action Buttons: "+ Qarshii Seene" & "- Qarshii Bahe"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddIncome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IncomeGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_add_income_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ ${strings.cashIn}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onAddExpense,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExpenseRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_add_expense_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "- ${strings.cashOut}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
