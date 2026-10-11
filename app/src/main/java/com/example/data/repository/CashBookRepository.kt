package com.example.data.repository

import com.example.data.local.CashBookDao
import com.example.data.model.CashBookEntity
import com.example.data.model.CurrencyCode
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CashBookRepository(private val dao: CashBookDao) {

    fun getTransactionsByBook(bookId: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsByBook(bookId)

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        dao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(id)
    }

    // Books
    fun getAllBooks(): Flow<List<CashBookEntity>> = dao.getAllBooks()

    suspend fun insertBook(book: CashBookEntity): Long = withContext(Dispatchers.IO) {
        dao.insertBook(book)
    }

    suspend fun updateBook(book: CashBookEntity) = withContext(Dispatchers.IO) {
        dao.updateBook(book)
    }

    suspend fun deleteBook(book: CashBookEntity) = withContext(Dispatchers.IO) {
        dao.deleteTransactionsByBook(book.id)
        dao.deleteBook(book)
    }

    // Debts
    fun getDebtsByBook(bookId: Long): Flow<List<DebtEntity>> = dao.getDebtsByBook(bookId)

    fun getAllDebts(): Flow<List<DebtEntity>> = dao.getAllDebts()

    suspend fun insertDebt(debt: DebtEntity): Long = withContext(Dispatchers.IO) {
        dao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        dao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        dao.deleteDebt(debt)
    }

    // Export entire database to JSON string
    suspend fun exportToJson(transactions: List<TransactionEntity>, debts: List<DebtEntity>, books: List<CashBookEntity>): String = withContext(Dispatchers.Default) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())
        root.put("appName", "Galmee Qarshii")

        // Books
        val booksArray = JSONArray()
        books.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("primaryCurrency", b.primaryCurrency)
                put("initialBalance", b.initialBalance)
                put("createdAt", b.createdAt)
            }
            booksArray.put(obj)
        }
        root.put("books", booksArray)

        // Transactions
        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("bookId", tx.bookId)
                put("type", tx.type)
                put("amount", tx.amount)
                put("currency", tx.currency)
                put("exchangeRateToPrimary", tx.exchangeRateToPrimary)
                put("category", tx.category)
                put("paymentMode", tx.paymentMode)
                put("timestamp", tx.timestamp)
                put("contactName", tx.contactName ?: "")
                put("notes", tx.notes ?: "")
                put("referenceNumber", tx.referenceNumber ?: "")
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Debts
        val debtsArray = JSONArray()
        debts.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("bookId", d.bookId)
                put("contactName", d.contactName)
                put("phoneNumber", d.phoneNumber ?: "")
                put("type", d.type)
                put("amount", d.amount)
                put("currency", d.currency)
                put("dueDate", d.dueDate ?: 0L)
                put("notes", d.notes ?: "")
                put("isSettled", d.isSettled)
                put("createdAt", d.createdAt)
            }
            debtsArray.put(obj)
        }
        root.put("debts", debtsArray)

        root.toString(2)
    }

    // Import from JSON string
    suspend fun importFromJson(jsonString: String, merge: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("transactions") && !root.has("books")) {
                return@withContext false
            }

            if (!merge) {
                dao.clearAllTransactions()
                dao.clearAllDebts()
            }

            // Import Books if present
            if (root.has("books")) {
                val booksArray = root.getJSONArray("books")
                val booksList = mutableListOf<CashBookEntity>()
                for (i in 0 until booksArray.length()) {
                    val obj = booksArray.getJSONObject(i)
                    booksList.add(
                        CashBookEntity(
                            id = if (merge) 0L else obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            primaryCurrency = obj.optString("primaryCurrency", "ETB"),
                            initialBalance = obj.optDouble("initialBalance", 0.0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (booksList.isNotEmpty()) {
                    dao.insertBooks(booksList)
                }
            }

            // Import Transactions
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                val txList = mutableListOf<TransactionEntity>()
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    txList.add(
                        TransactionEntity(
                            id = if (merge) 0L else obj.optLong("id", 0L),
                            bookId = obj.optLong("bookId", 1L),
                            type = obj.getString("type"),
                            amount = obj.getDouble("amount"),
                            currency = obj.optString("currency", "ETB"),
                            exchangeRateToPrimary = obj.optDouble("exchangeRateToPrimary", 1.0),
                            category = obj.getString("category"),
                            paymentMode = obj.optString("paymentMode", "CASH"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            contactName = obj.optString("contactName").takeIf { it.isNotBlank() },
                            notes = obj.optString("notes").takeIf { it.isNotBlank() },
                            referenceNumber = obj.optString("referenceNumber").takeIf { it.isNotBlank() }
                        )
                    )
                }
                if (txList.isNotEmpty()) {
                    dao.insertTransactions(txList)
                }
            }

            // Import Debts
            if (root.has("debts")) {
                val debtsArray = root.getJSONArray("debts")
                val debtsList = mutableListOf<DebtEntity>()
                for (i in 0 until debtsArray.length()) {
                    val obj = debtsArray.getJSONObject(i)
                    debtsList.add(
                        DebtEntity(
                            id = if (merge) 0L else obj.optLong("id", 0L),
                            bookId = obj.optLong("bookId", 1L),
                            contactName = obj.getString("contactName"),
                            phoneNumber = obj.optString("phoneNumber").takeIf { it.isNotBlank() },
                            type = obj.getString("type"),
                            amount = obj.getDouble("amount"),
                            currency = obj.optString("currency", "ETB"),
                            dueDate = obj.optLong("dueDate", 0L).takeIf { it > 0L },
                            notes = obj.optString("notes").takeIf { it.isNotBlank() },
                            isSettled = obj.optBoolean("isSettled", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (debtsList.isNotEmpty()) {
                    dao.insertDebts(debtsList)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Export to CSV format (Spreadsheet readable)
    fun exportToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Time,Type,Amount,Currency,AmountInPrimary,Category,PaymentMode,Contact,Notes,Reference\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        transactions.forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val timeStr = timeFormat.format(Date(tx.timestamp))
            val contact = (tx.contactName ?: "").replace("\"", "\"\"")
            val notes = (tx.notes ?: "").replace("\"", "\"\"")
            val ref = (tx.referenceNumber ?: "").replace("\"", "\"\"")

            sb.append("${tx.id},")
            sb.append("$dateStr,")
            sb.append("$timeStr,")
            sb.append("${tx.type},")
            sb.append("${tx.amount},")
            sb.append("${tx.currency},")
            sb.append("${tx.amountInPrimary},")
            sb.append("${tx.category},")
            sb.append("${tx.paymentMode},")
            sb.append("\"$contact\",")
            sb.append("\"$notes\",")
            sb.append("\"$ref\"\n")
        }
        return sb.toString()
    }
}
