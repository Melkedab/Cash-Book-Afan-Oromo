package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCode
import com.example.localization.AppStrings
import com.example.ui.CategorySummary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun ReportsScreen(
    strings: AppStrings,
    totalIncome: Double,
    totalExpense: Double,
    netBalance: Double,
    primaryCurrency: CurrencyCode,
    privacyMode: Boolean,
    categorySummaries: List<CategorySummary>,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVolume = totalIncome + totalExpense
    val incomePercent = if (totalVolume > 0) (totalIncome / totalVolume).toFloat() else 0.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.financialSummary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = onShareReport,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("share_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.shareReport, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Ratio Bar
                    Text(
                        text = strings.incomeVsExpense,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(if (incomePercent > 0.05f) incomePercent else 0.05f)
                                .fillMaxSize()
                                .background(IncomeGreen)
                        )
                        Box(
                            modifier = Modifier
                                .weight(if ((1f - incomePercent) > 0.05f) (1f - incomePercent) else 0.05f)
                                .fillMaxSize()
                                .background(ExpenseRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.cashIn}: ${primaryCurrency.symbol} ${if (privacyMode) "••" else String.format(Locale.US, "%,.0f", totalIncome)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IncomeGreen
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.cashOut}: ${primaryCurrency.symbol} ${if (privacyMode) "••" else String.format(Locale.US, "%,.0f", totalExpense)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Category Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.expenseBreakdown,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (categorySummaries.isEmpty()) {
                        Text(
                            text = strings.noTransactionsFound,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categorySummaries.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.categoryName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${primaryCurrency.symbol} ${if (privacyMode) "••" else String.format(Locale.US, "%,.0f", item.amount)} (${String.format(Locale.US, "%.1f", item.percentage)}%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { item.percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = ExpenseRed,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}
