package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long = 1L,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val currency: String = "ETB",
    val exchangeRateToPrimary: Double = 1.0, // Multiplier to convert to primary currency
    val category: String,
    val paymentMode: String = "CASH",
    val timestamp: Long = System.currentTimeMillis(),
    val contactName: String? = null,
    val notes: String? = null,
    val referenceNumber: String? = null
) {
    val amountInPrimary: Double
        get() = amount * exchangeRateToPrimary
}

@Entity(tableName = "cash_books")
data class CashBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryCurrency: String = "ETB",
    val initialBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_records")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long = 1L,
    val contactName: String,
    val phoneNumber: String? = null,
    val type: String, // "TO_RECEIVE" (Qaba) or "TO_PAY" (Narra jira)
    val amount: Double,
    val currency: String = "ETB",
    val dueDate: Long? = null,
    val notes: String? = null,
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
