package com.example.data.model

import com.example.localization.AppLanguage

enum class CurrencyCode(
    val code: String,
    val symbol: String,
    val nameEn: String,
    val nameOm: String,
    val defaultRateToETB: Double // How many ETB per 1 unit of this currency
) {
    ETB("ETB", "Br", "Ethiopian Birr", "Qarshii Itoophiyaa", 1.0),
    USD("USD", "$", "US Dollar", "Doolaara Ameerikaa", 130.0),
    EUR("EUR", "€", "Euro", "Yuuroo Awurooppaa", 142.0),
    GBP("GBP", "£", "British Pound", "Paawondii Ingilizii", 168.0),
    KES("KES", "KSh", "Kenyan Shilling", "Shiiliingii Keeniyaa", 1.01),
    SAR("SAR", "SR", "Saudi Riyal", "Riyaala Sa'udii", 34.6),
    AED("AED", "AED", "UAE Dirham", "Dirhaama Emireetotaa", 35.4),
    CAD("CAD", "CA$", "Canadian Dollar", "Doolaara Kanaadaa", 95.0);

    fun getDisplayName(language: AppLanguage): String {
        return when (language) {
            AppLanguage.AFAAN_OROMOO -> "$code ($symbol) - $nameOm"
            AppLanguage.ENGLISH -> "$code ($symbol) - $nameEn"
        }
    }

    companion object {
        fun fromCode(code: String): CurrencyCode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ETB
        }
    }
}

enum class TransactionType(val code: String) {
    INCOME("INCOME"),
    EXPENSE("EXPENSE")
}

enum class PaymentMode(val code: String) {
    CASH("CASH"),
    TELEBIRR("TELEBIRR"),
    CBE_BIRR("CBE_BIRR"),
    BANK("BANK"),
    CARD("CARD"),
    OTHER("OTHER")
}

data class TransactionCategory(
    val id: String,
    val nameOm: String,
    val nameEn: String,
    val iconName: String,
    val type: TransactionType
) {
    fun getDisplayName(language: AppLanguage): String {
        return if (language == AppLanguage.AFAAN_OROMOO) nameOm else nameEn
    }

    companion object {
        val DefaultIncomeCategories = listOf(
            TransactionCategory("sales", "Gurgurtaa / Daldala", "Sales & Trade", "ShoppingBag", TransactionType.INCOME),
            TransactionCategory("salary", "Miindaa / Mindaa", "Salary & Wages", "Work", TransactionType.INCOME),
            TransactionCategory("service", "Tajaajila / Hojii", "Service Fee", "Build", TransactionType.INCOME),
            TransactionCategory("rent_income", "Kiree Galii", "Rental Income", "Home", TransactionType.INCOME),
            TransactionCategory("investment", "Dhaloota / Qabeenya", "Investment / Profit", "TrendingUp", TransactionType.INCOME),
            TransactionCategory("gift_income", "Kennaa / Gargaarsa", "Gift / Remittance", "CardGiftcard", TransactionType.INCOME),
            TransactionCategory("other_income", "Galii Biraa", "Other Income", "AttachMoney", TransactionType.INCOME)
        )

        val DefaultExpenseCategories = listOf(
            TransactionCategory("food", "Nyaata & Dhugaatii", "Food & Dining", "Restaurant", TransactionType.EXPENSE),
            TransactionCategory("groceries", "Bittaa Manaa / Suuqii", "Groceries & Market", "ShoppingCart", TransactionType.EXPENSE),
            TransactionCategory("transport", "Geejjiba / Baabura", "Transport & Fuel", "DirectionsCar", TransactionType.EXPENSE),
            TransactionCategory("rent_expense", "Kiree Manaa / Bakkaa", "Rent & Housing", "Home", TransactionType.EXPENSE),
            TransactionCategory("bills", "Kaffaltii Tajaajilaa (Ibsaa/Bishaan)", "Bills & Utilities", "Receipt", TransactionType.EXPENSE),
            TransactionCategory("health", "Fayyaa & Qoricha", "Health & Pharmacy", "LocalHospital", TransactionType.EXPENSE),
            TransactionCategory("education", "Barnoota / Kitaaba", "Education & Books", "School", TransactionType.EXPENSE),
            TransactionCategory("shopping", "Uffata & Qabeenya", "Clothing & Personal", "Checkroom", TransactionType.EXPENSE),
            TransactionCategory("maintenance", "Suphaa & Qulqullina", "Maintenance & Repairs", "Handyman", TransactionType.EXPENSE),
            TransactionCategory("debt_paid", "Liqii Deebisuu", "Debt Repayment", "MoneyOff", TransactionType.EXPENSE),
            TransactionCategory("other_expense", "Baasii Biraa", "Other Expense", "Payment", TransactionType.EXPENSE)
        )

        fun findCategory(id: String, type: TransactionType): TransactionCategory {
            val list = if (type == TransactionType.INCOME) DefaultIncomeCategories else DefaultExpenseCategories
            return list.firstOrNull { it.id == id } ?: list.last()
        }
    }
}
