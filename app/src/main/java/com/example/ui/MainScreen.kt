package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.localization.AppLanguage
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.BackupSyncScreen
import com.example.ui.screens.CurrencyConverterScreen
import com.example.ui.screens.DailyLedgerScreen
import com.example.ui.screens.DebtLedgerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary

enum class MainTab(val titleOm: String, val titleEn: String) {
    LEDGER("Galmee", "Ledger"),
    DEBT("Liqii", "Debts"),
    REPORTS("Gabaasa", "Reports"),
    CONVERTER("Jijjiirraa", "Convert"),
    SETTINGS("Sajoo", "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CashBookViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val strings by viewModel.strings.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val pinCode by viewModel.pinCode.collectAsStateWithLifecycle()
    val primaryCurrency by viewModel.primaryCurrency.collectAsStateWithLifecycle()
    val exchangeRates by viewModel.exchangeRates.collectAsStateWithLifecycle()

    val activeBookId by viewModel.activeBookId.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val activeBook = remember(allBooks, activeBookId) {
        allBooks.firstOrNull { it.id == activeBookId } ?: allBooks.firstOrNull()
    }

    val dailyGroups by viewModel.dailyLedgerGroups.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val netBalance by viewModel.netBalance.collectAsStateWithLifecycle()

    val debts by viewModel.activeDebts.collectAsStateWithLifecycle()
    val totalReceivable by viewModel.totalReceivable.collectAsStateWithLifecycle()
    val totalPayable by viewModel.totalPayable.collectAsStateWithLifecycle()

    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categorySummaries by viewModel.expenseCategoriesSummary.collectAsStateWithLifecycle()

    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    var currentTab by remember { mutableStateOf(MainTab.LEDGER) }
    var isBackupSyncOpen by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var addSheetInitialType by remember { mutableStateOf("INCOME") }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // If PIN is enabled and locked, show PIN entry screen
    if (!isUnlocked && pinCode.isNotEmpty()) {
        PinLockScreen(
            strings = strings,
            isSettingPin = false,
            onPinSuccess = { input ->
                viewModel.verifyPin(input)
            }
        )
        return
    }

    // Handle back button when on sub-screens
    if (isBackupSyncOpen) {
        BackHandler {
            isBackupSyncOpen = false
        }
    } else if (currentTab != MainTab.LEDGER) {
        BackHandler {
            currentTab = MainTab.LEDGER
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.appName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Language switcher button (Quick toggle between Afaan Oromoo and English)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val next = if (language == AppLanguage.AFAAN_OROMOO) AppLanguage.ENGLISH else AppLanguage.AFAAN_OROMOO
                                viewModel.setLanguage(next)
                            }
                            .testTag("quick_lang_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.AFAAN_OROMOO) "Afaan Oromoo" else "English",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Privacy Toggle
                    IconButton(
                        onClick = { viewModel.togglePrivacyMode() },
                        modifier = Modifier.testTag("topbar_privacy_button")
                    ) {
                        Icon(
                            imageVector = if (privacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Privacy",
                            tint = if (privacyMode) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (currentTab == MainTab.LEDGER) {
                FloatingActionButton(
                    onClick = {
                        editingTransaction = null
                        addSheetInitialType = "INCOME"
                        showAddSheet = true
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("main_add_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.addTransaction)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // 1. Ledger
                NavigationBarItem(
                    selected = currentTab == MainTab.LEDGER,
                    onClick = { currentTab = MainTab.LEDGER },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = {
                        Text(
                            text = if (language == AppLanguage.AFAAN_OROMOO) MainTab.LEDGER.titleOm else MainTab.LEDGER.titleEn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.LEDGER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_ledger")
                )

                // 2. Debts (Liqii)
                NavigationBarItem(
                    selected = currentTab == MainTab.DEBT,
                    onClick = { currentTab = MainTab.DEBT },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = {
                        Text(
                            text = if (language == AppLanguage.AFAAN_OROMOO) MainTab.DEBT.titleOm else MainTab.DEBT.titleEn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.DEBT) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_debt")
                )

                // 3. Reports
                NavigationBarItem(
                    selected = currentTab == MainTab.REPORTS,
                    onClick = { currentTab = MainTab.REPORTS },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
                    label = {
                        Text(
                            text = if (language == AppLanguage.AFAAN_OROMOO) MainTab.REPORTS.titleOm else MainTab.REPORTS.titleEn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.REPORTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_reports")
                )

                // 4. Currency Converter
                NavigationBarItem(
                    selected = currentTab == MainTab.CONVERTER,
                    onClick = { currentTab = MainTab.CONVERTER },
                    icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) },
                    label = {
                        Text(
                            text = if (language == AppLanguage.AFAAN_OROMOO) MainTab.CONVERTER.titleOm else MainTab.CONVERTER.titleEn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.CONVERTER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_converter")
                )

                // 5. Settings / Backup
                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { currentTab = MainTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = {
                        Text(
                            text = if (language == AppLanguage.AFAAN_OROMOO) MainTab.SETTINGS.titleOm else MainTab.SETTINGS.titleEn,
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == MainTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                MainTab.LEDGER -> {
                    DailyLedgerScreen(
                        strings = strings,
                        language = language,
                        netBalance = netBalance,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        primaryCurrency = primaryCurrency,
                        privacyMode = privacyMode,
                        onTogglePrivacy = { viewModel.togglePrivacyMode() },
                        activeBook = activeBook,
                        allBooks = allBooks,
                        onSelectBook = { viewModel.setActiveBook(it) },
                        onCreateNewBook = { currentTab = MainTab.SETTINGS },
                        dailyGroups = dailyGroups,
                        dateFilter = dateFilter,
                        onDateFilterChange = { viewModel.setDateFilter(it) },
                        typeFilter = typeFilter,
                        onTypeFilterChange = { viewModel.setTypeFilter(it) },
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onAddIncome = {
                            editingTransaction = null
                            addSheetInitialType = "INCOME"
                            showAddSheet = true
                        },
                        onAddExpense = {
                            editingTransaction = null
                            addSheetInitialType = "EXPENSE"
                            showAddSheet = true
                        },
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            addSheetInitialType = tx.type
                            showAddSheet = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        }
                    )
                }

                MainTab.DEBT -> {
                    DebtLedgerScreen(
                        strings = strings,
                        debts = debts,
                        totalReceivable = totalReceivable,
                        totalPayable = totalPayable,
                        primaryCurrency = primaryCurrency,
                        privacyMode = privacyMode,
                        onAddDebt = { contact, phone, type, amt, curr, due, notes ->
                            viewModel.addDebt(contact, phone, type, amt, curr, due, notes)
                        },
                        onToggleSettled = { viewModel.toggleDebtSettled(it) },
                        onDeleteDebt = { viewModel.deleteDebt(it) }
                    )
                }

                MainTab.REPORTS -> {
                    ReportsScreen(
                        strings = strings,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        netBalance = netBalance,
                        primaryCurrency = primaryCurrency,
                        privacyMode = privacyMode,
                        categorySummaries = categorySummaries,
                        onShareReport = {
                            val text = viewModel.getShareableReportText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, strings.shareReport))
                        }
                    )
                }

                MainTab.CONVERTER -> {
                    CurrencyConverterScreen(
                        strings = strings,
                        language = language,
                        exchangeRates = exchangeRates,
                        onUpdateRate = { code, rate ->
                            viewModel.updateExchangeRate(code, rate)
                        }
                    )
                }

                MainTab.SETTINGS -> {
                    if (isBackupSyncOpen) {
                        BackupSyncScreen(
                            strings = strings,
                            onExportJson = { viewModel.exportJsonBackup() },
                            onImportJson = { json, merge -> viewModel.importJsonBackup(json, merge) },
                            onExportCsv = { viewModel.exportCsvData() },
                            onShareSummary = {
                                val text = viewModel.getShareableReportText()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, strings.shareReport))
                            }
                        )
                    } else {
                        SettingsScreen(
                            strings = strings,
                            currentLanguage = language,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            primaryCurrency = primaryCurrency,
                            onSelectPrimaryCurrency = { viewModel.setPrimaryCurrency(it) },
                            privacyMode = privacyMode,
                            onTogglePrivacy = { viewModel.togglePrivacyMode() },
                            pinCode = pinCode,
                            onSetPin = { viewModel.setSecurityPin(it) },
                            activeBook = activeBook,
                            allBooks = allBooks,
                            onSelectBook = { viewModel.setActiveBook(it) },
                            onCreateBook = { name, curr -> viewModel.createBook(name, curr) },
                            onOpenBackupSync = { isBackupSyncOpen = true }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Transaction BottomSheet
    if (showAddSheet) {
        AddTransactionSheet(
            initialType = addSheetInitialType,
            existingTransaction = editingTransaction,
            strings = strings,
            language = language,
            primaryCurrency = primaryCurrency,
            exchangeRates = exchangeRates,
            onDismiss = {
                showAddSheet = false
                editingTransaction = null
            },
            onSave = { type, amount, currency, category, mode, timestamp, contact, notes, ref ->
                if (editingTransaction != null) {
                    val updated = editingTransaction!!.copy(
                        type = type,
                        amount = amount,
                        currency = currency,
                        category = category,
                        paymentMode = mode,
                        timestamp = timestamp,
                        contactName = contact,
                        notes = notes,
                        referenceNumber = ref
                    )
                    viewModel.updateTransaction(updated)
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        currency = currency,
                        category = category,
                        paymentMode = mode,
                        timestamp = timestamp,
                        contactName = contact,
                        notes = notes,
                        referenceNumber = ref
                    )
                }
                showAddSheet = false
                editingTransaction = null
            }
        )
    }
}
