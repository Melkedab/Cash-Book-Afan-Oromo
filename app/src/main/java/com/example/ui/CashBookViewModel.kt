package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CashBookDatabase
import com.example.data.model.CashBookEntity
import com.example.data.model.CurrencyCode
import com.example.data.model.DebtEntity
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.CashBookRepository
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.localization.LocalizationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterOption {
    ALL, TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH
}

enum class TypeFilterOption {
    ALL, INCOME, EXPENSE
}

data class DailyLedgerGroup(
    val dateKey: String, // "YYYY-MM-DD"
    val dateDisplay: String, // e.g. "Har'a, 11 Onkololeessa"
    val dayTimestamp: Long,
    val cashInTotal: Double,
    val cashOutTotal: Double,
    val netDayBalance: Double,
    val transactions: List<TransactionEntity>
)

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

class CashBookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CashBookRepository

    private val prefs = application.getSharedPreferences("cashbook_prefs", Context.MODE_PRIVATE)

    // Language
    private val _language = MutableStateFlow(
        if (prefs.getString("app_lang", "om") == "en") AppLanguage.ENGLISH else AppLanguage.AFAAN_OROMOO
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Privacy Mode (Hide/Show balances)
    private val _privacyMode = MutableStateFlow(prefs.getBoolean("privacy_mode", false))
    val privacyMode: StateFlow<Boolean> = _privacyMode.asStateFlow()

    // Security PIN
    private val _pinCode = MutableStateFlow(prefs.getString("security_pin", "") ?: "")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _isUnlocked = MutableStateFlow(_pinCode.value.isEmpty())
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Primary Currency
    private val _primaryCurrency = MutableStateFlow(
        CurrencyCode.fromCode(prefs.getString("primary_currency", "ETB") ?: "ETB")
    )
    val primaryCurrency: StateFlow<CurrencyCode> = _primaryCurrency.asStateFlow()

    // Custom Exchange Rates (relative to ETB)
    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(
        CurrencyCode.entries.associate { it.code to it.defaultRateToETB }.toMutableMap()
    )
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates.asStateFlow()

    // Active Book ID
    private val _activeBookId = MutableStateFlow(prefs.getLong("active_book_id", 1L))
    val activeBookId: StateFlow<Long> = _activeBookId.asStateFlow()

    // Filtering & Searching
    private val _dateFilter = MutableStateFlow(DateFilterOption.ALL)
    val dateFilter: StateFlow<DateFilterOption> = _dateFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow(TypeFilterOption.ALL)
    val typeFilter: StateFlow<TypeFilterOption> = _typeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Toast / Feedback message
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        val database = CashBookDatabase.getDatabase(application, viewModelScope)
        repository = CashBookRepository(database.cashBookDao())
    }

    val strings: StateFlow<AppStrings> = _language.combine(_privacyMode) { lang, _ ->
        LocalizationManager.get(lang)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LocalizationManager.get(_language.value))

    // Cash Books
    val allBooks: StateFlow<List<CashBookEntity>> = repository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All raw transactions for the active book
    val rawTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for active book and filters
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        rawTransactions,
        _activeBookId,
        _dateFilter,
        _typeFilter,
        _searchQuery
    ) { txList, bookId, dateF, typeF, query ->
        txList.filter { tx ->
            val matchesBook = (tx.bookId == bookId || (bookId == 1L && tx.bookId == 0L))
            val matchesType = when (typeF) {
                TypeFilterOption.ALL -> true
                TypeFilterOption.INCOME -> tx.type == "INCOME"
                TypeFilterOption.EXPENSE -> tx.type == "EXPENSE"
            }
            val matchesDate = isWithinDateFilter(tx.timestamp, dateF)
            val matchesSearch = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                (tx.notes?.lowercase()?.contains(q) == true) ||
                        (tx.contactName?.lowercase()?.contains(q) == true) ||
                        (tx.referenceNumber?.lowercase()?.contains(q) == true) ||
                        (tx.category.lowercase().contains(q)) ||
                        (tx.amount.toString().contains(q))
            }
            matchesBook && matchesType && matchesDate && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Ledger grouping
    val dailyLedgerGroups: StateFlow<List<DailyLedgerGroup>> = combine(
        filteredTransactions,
        _language
    ) { txList, lang ->
        val groups = txList.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }

        groups.map { (dateKey, items) ->
            val firstTime = items.firstOrNull()?.timestamp ?: System.currentTimeMillis()
            var cashIn = 0.0
            var cashOut = 0.0
            items.forEach { tx ->
                val amt = tx.amountInPrimary
                if (tx.type == "INCOME") cashIn += amt else cashOut += amt
            }
            DailyLedgerGroup(
                dateKey = dateKey,
                dateDisplay = formatGroupDate(firstTime, lang),
                dayTimestamp = firstTime,
                cashInTotal = cashIn,
                cashOutTotal = cashOut,
                netDayBalance = cashIn - cashOut,
                transactions = items
            )
        }.sortedByDescending { it.dayTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Totals
    val totalIncome: StateFlow<Double> = filteredTransactions.combine(_primaryCurrency) { txs, _ ->
        txs.filter { it.type == "INCOME" }.sumOf { it.amountInPrimary }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = filteredTransactions.combine(_primaryCurrency) { txs, _ ->
        txs.filter { it.type == "EXPENSE" }.sumOf { it.amountInPrimary }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Debts
    val rawDebts: StateFlow<List<DebtEntity>> = repository.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDebts: StateFlow<List<DebtEntity>> = combine(rawDebts, _activeBookId) { debts, bookId ->
        debts.filter { it.bookId == bookId || (bookId == 1L && it.bookId == 0L) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReceivable: StateFlow<Double> = activeDebts.combine(_exchangeRates) { debts, rates ->
        debts.filter { it.type == "TO_RECEIVE" && !it.isSettled }
            .sumOf { it.amount * (rates[it.currency] ?: 1.0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPayable: StateFlow<Double> = activeDebts.combine(_exchangeRates) { debts, rates ->
        debts.filter { it.type == "TO_PAY" && !it.isSettled }
            .sumOf { it.amount * (rates[it.currency] ?: 1.0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Expense Categories Breakdown for charts
    val expenseCategoriesSummary: StateFlow<List<CategorySummary>> = combine(
        filteredTransactions,
        _language
    ) { txs, lang ->
        val expenses = txs.filter { it.type == "EXPENSE" }
        val totalExp = expenses.sumOf { it.amountInPrimary }
        if (totalExp == 0.0) return@combine emptyList()

        val byCategory = expenses.groupBy { it.category }
        byCategory.map { (catId, list) ->
            val sum = list.sumOf { it.amountInPrimary }
            val catObj = TransactionCategory.findCategory(catId, TransactionType.EXPENSE)
            CategorySummary(
                categoryId = catId,
                categoryName = catObj.getDisplayName(lang),
                amount = sum,
                percentage = (sum / totalExp * 100).toFloat()
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("app_lang", lang.code).apply()
    }

    fun togglePrivacyMode() {
        val newVal = !_privacyMode.value
        _privacyMode.value = newVal
        prefs.edit().putBoolean("privacy_mode", newVal).apply()
    }

    fun setSecurityPin(pin: String) {
        _pinCode.value = pin
        prefs.edit().putString("security_pin", pin).apply()
        _isUnlocked.value = true
        _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Koodiin PIN eegumsaa saajameera" else "Security PIN saved"
    }

    fun verifyPin(input: String): Boolean {
        if (_pinCode.value.isEmpty() || input == _pinCode.value) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun lockApp() {
        if (_pinCode.value.isNotEmpty()) {
            _isUnlocked.value = false
        }
    }

    fun setPrimaryCurrency(currency: CurrencyCode) {
        _primaryCurrency.value = currency
        prefs.edit().putString("primary_currency", currency.code).apply()
    }

    fun updateExchangeRate(currencyCode: String, rate: Double) {
        val updated = _exchangeRates.value.toMutableMap()
        updated[currencyCode] = rate
        _exchangeRates.value = updated
        _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Shallaggiin jijjiirraa haaromeera" else "Exchange rate updated"
    }

    fun setActiveBook(bookId: Long) {
        _activeBookId.value = bookId
        prefs.edit().putLong("active_book_id", bookId).apply()
    }

    fun setDateFilter(filter: DateFilterOption) {
        _dateFilter.value = filter
    }

    fun setTypeFilter(filter: TypeFilterOption) {
        _typeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // CRUD Transactions
    fun addTransaction(
        type: String,
        amount: Double,
        currency: String,
        category: String,
        paymentMode: String,
        timestamp: Long,
        contactName: String?,
        notes: String?,
        referenceNumber: String?
    ) {
        viewModelScope.launch {
            val rate = _exchangeRates.value[currency] ?: 1.0
            val tx = TransactionEntity(
                bookId = _activeBookId.value,
                type = type,
                amount = amount,
                currency = currency,
                exchangeRateToPrimary = rate,
                category = category,
                paymentMode = paymentMode,
                timestamp = timestamp,
                contactName = contactName?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null },
                referenceNumber = referenceNumber?.trim()?.ifBlank { null }
            )
            repository.insertTransaction(tx)
            _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Sochiin qarshii galmeeffameera!" else "Transaction saved!"
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
            _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Sochiin sirreeffameera!" else "Transaction updated!"
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Sochiin haqameera!" else "Transaction deleted!"
        }
    }

    // CRUD Debts
    fun addDebt(
        contactName: String,
        phoneNumber: String?,
        type: String,
        amount: Double,
        currency: String,
        dueDate: Long?,
        notes: String?
    ) {
        viewModelScope.launch {
            val debt = DebtEntity(
                bookId = _activeBookId.value,
                contactName = contactName.trim(),
                phoneNumber = phoneNumber?.trim()?.ifBlank { null },
                type = type,
                amount = amount,
                currency = currency,
                dueDate = dueDate,
                notes = notes?.trim()?.ifBlank { null },
                isSettled = false
            )
            repository.insertDebt(debt)
            _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Galmeen liqii ooleera!" else "Debt record saved!"
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch {
            val updated = debt.copy(isSettled = !debt.isSettled)
            repository.updateDebt(updated)
            _feedbackMessage.value = if (updated.isSettled) {
                if (_language.value == AppLanguage.AFAAN_OROMOO) "Kaffalamee xumurameera!" else "Marked as settled!"
            } else {
                if (_language.value == AppLanguage.AFAAN_OROMOO) "Hafteetti deebifameera" else "Marked as pending"
            }
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
            _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Galmeen liqii haqameera" else "Debt deleted"
        }
    }

    // Cash Books
    fun createBook(name: String, primaryCurrency: String = "ETB") {
        viewModelScope.launch {
            val id = repository.insertBook(
                CashBookEntity(
                    name = name.trim(),
                    primaryCurrency = primaryCurrency,
                    initialBalance = 0.0
                )
            )
            setActiveBook(id)
            _feedbackMessage.value = if (_language.value == AppLanguage.AFAAN_OROMOO) "Galmee haaraan banameera!" else "New Cash Book created!"
        }
    }

    // Backup & Sync
    suspend fun exportJsonBackup(): String {
        return repository.exportToJson(
            transactions = rawTransactions.value,
            debts = rawDebts.value,
            books = allBooks.value
        )
    }

    suspend fun importJsonBackup(jsonString: String, merge: Boolean = false): Boolean {
        val success = repository.importFromJson(jsonString, merge)
        _feedbackMessage.value = if (success) {
            if (_language.value == AppLanguage.AFAAN_OROMOO) "Daataan milkaa'inaan deebifameera!" else "Backup restored successfully!"
        } else {
            if (_language.value == AppLanguage.AFAAN_OROMOO) "Dogoggora: Faayilii sirrii miti" else "Error: Invalid backup file"
        }
        return success
    }

    fun exportCsvData(): String {
        return repository.exportToCsv(filteredTransactions.value)
    }

    fun getShareableReportText(): String {
        val lang = _language.value
        val str = LocalizationManager.get(lang)
        val symbol = _primaryCurrency.value.symbol
        val sb = StringBuilder()
        sb.append("📋 ${str.appName} - ${str.financialSummary}\n")
        sb.append("----------------------------\n")
        sb.append("${str.totalIncome}: $symbol ${String.format(Locale.US, "%,.2f", totalIncome.value)}\n")
        sb.append("${str.totalExpense}: $symbol ${String.format(Locale.US, "%,.2f", totalExpense.value)}\n")
        sb.append("${str.netBalance}: $symbol ${String.format(Locale.US, "%,.2f", netBalance.value)}\n\n")

        sb.append("💰 ${str.debtTitle}:\n")
        sb.append("${str.totalReceivable}: $symbol ${String.format(Locale.US, "%,.2f", totalReceivable.value)}\n")
        sb.append("${str.totalPayable}: $symbol ${String.format(Locale.US, "%,.2f", totalPayable.value)}\n\n")

        sb.append("📅 ${str.dailyLedger} (${filteredTransactions.value.size} ${str.transactions}):\n")
        dailyLedgerGroups.value.take(5).forEach { group ->
            sb.append("• ${group.dateDisplay}: +$symbol${String.format(Locale.US, "%,.0f", group.cashInTotal)} | -$symbol${String.format(Locale.US, "%,.0f", group.cashOutTotal)}\n")
        }
        sb.append("\n${str.offlineSyncNotice}")
        return sb.toString()
    }

    private fun isWithinDateFilter(timestamp: Long, filter: DateFilterOption): Boolean {
        if (filter == DateFilterOption.ALL) return true
        val calNow = Calendar.getInstance()
        val calTx = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when (filter) {
            DateFilterOption.TODAY -> {
                calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                        calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)
            }
            DateFilterOption.YESTERDAY -> {
                val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                calYesterday.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                        calYesterday.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)
            }
            DateFilterOption.THIS_WEEK -> {
                calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                        calNow.get(Calendar.WEEK_OF_YEAR) == calTx.get(Calendar.WEEK_OF_YEAR)
            }
            DateFilterOption.THIS_MONTH -> {
                calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                        calNow.get(Calendar.MONTH) == calTx.get(Calendar.MONTH)
            }
            DateFilterOption.ALL -> true
        }
    }

    private fun formatGroupDate(timestamp: Long, language: AppLanguage): String {
        val calNow = Calendar.getInstance()
        val calTx = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)
        if (isToday) {
            return if (language == AppLanguage.AFAAN_OROMOO) "Har'a" else "Today"
        }

        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = calYesterday.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calYesterday.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) {
            return if (language == AppLanguage.AFAAN_OROMOO) "Kaleessa" else "Yesterday"
        }

        val day = calTx.get(Calendar.DAY_OF_MONTH)
        val monthIdx = calTx.get(Calendar.MONTH)
        val year = calTx.get(Calendar.YEAR)

        val oromoMonths = listOf(
            "Amajjii", "Guraandhala", "Bitooteessa", "Elba", "Waxabajjii",
            "Adooleessa", "Hagayya", "Fuulbaana", "Onkololeessa", "Sadaasa", "Muddee"
        )
        val englishMonths = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        return if (language == AppLanguage.AFAAN_OROMOO) {
            val monthName = oromoMonths.getOrElse(monthIdx) { "Ji'a ${monthIdx + 1}" }
            "$day $monthName $year"
        } else {
            val monthName = englishMonths.getOrElse(monthIdx) { "" }
            "$monthName $day, $year"
        }
    }
}
