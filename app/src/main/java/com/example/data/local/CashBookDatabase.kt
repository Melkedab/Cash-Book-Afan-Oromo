package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CashBookEntity
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CashBookEntity::class, DebtEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CashBookDatabase : RoomDatabase() {

    abstract fun cashBookDao(): CashBookDao

    companion object {
        @Volatile
        private var INSTANCE: CashBookDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CashBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CashBookDatabase::class.java,
                    "galmee_qarshii_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.cashBookDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: CashBookDao) {
                // Initial Default Cash Book
                val defaultBookId = dao.insertBook(
                    CashBookEntity(
                        id = 1L,
                        name = "Galmee Daldalaa (Business)",
                        primaryCurrency = "ETB",
                        initialBalance = 15000.0,
                        createdAt = System.currentTimeMillis()
                    )
                )

                // Second book for personal expenses
                dao.insertBook(
                    CashBookEntity(
                        id = 2L,
                        name = "Galmee Dhuunfaa (Personal)",
                        primaryCurrency = "ETB",
                        initialBalance = 5000.0,
                        createdAt = System.currentTimeMillis()
                    )
                )

                val now = System.currentTimeMillis()
                val oneDay = 24 * 60 * 60 * 1000L

                // Initial realistic transactions in Afaan Oromo
                dao.insertTransaction(
                    TransactionEntity(
                        bookId = defaultBookId,
                        type = "INCOME",
                        amount = 4500.0,
                        currency = "ETB",
                        exchangeRateToPrimary = 1.0,
                        category = "sales",
                        paymentMode = "TELEBIRR",
                        timestamp = now - (oneDay * 2) + 3600000,
                        contactName = "Caalaa Mokonnin",
                        notes = "Gurgurtaa meeshaa daldalaa",
                        referenceNumber = "REF-8941"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        bookId = defaultBookId,
                        type = "EXPENSE",
                        amount = 850.0,
                        currency = "ETB",
                        exchangeRateToPrimary = 1.0,
                        category = "transport",
                        paymentMode = "CASH",
                        timestamp = now - (oneDay * 2) + 7200000,
                        contactName = "Konkolaachisaa",
                        notes = "Geejjiba meeshaa fe'uu",
                        referenceNumber = "RCP-102"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        bookId = defaultBookId,
                        type = "INCOME",
                        amount = 12000.0,
                        currency = "ETB",
                        exchangeRateToPrimary = 1.0,
                        category = "service",
                        paymentMode = "CBE_BIRR",
                        timestamp = now - oneDay + 1800000,
                        contactName = "Obboo Tolasaa",
                        notes = "Kaffaltii tajaajila suphaa",
                        referenceNumber = "CBE-5519"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        bookId = defaultBookId,
                        type = "EXPENSE",
                        amount = 1200.0,
                        currency = "ETB",
                        exchangeRateToPrimary = 1.0,
                        category = "groceries",
                        paymentMode = "CASH",
                        timestamp = now - 3600000 * 5,
                        contactName = "Suuqii Aliyyii",
                        notes = "Meeshaalee daldalaa dabalataa",
                        referenceNumber = "RCP-119"
                    )
                )

                dao.insertTransaction(
                    TransactionEntity(
                        bookId = defaultBookId,
                        type = "INCOME",
                        amount = 50.0,
                        currency = "USD",
                        exchangeRateToPrimary = 130.0,
                        category = "gift_income",
                        paymentMode = "BANK",
                        timestamp = now - 3600000 * 2,
                        contactName = "Hawi (USA)",
                        notes = "Gargaarsa maatii doolaaraan",
                        referenceNumber = "WIRE-441"
                    )
                )

                // Sample debt record
                dao.insertDebt(
                    DebtEntity(
                        bookId = defaultBookId,
                        contactName = "Girmaa Tulluu",
                        phoneNumber = "+251911223344",
                        type = "TO_RECEIVE", // Qaba
                        amount = 3500.0,
                        currency = "ETB",
                        dueDate = now + (oneDay * 5),
                        notes = "Gurgurtaa kireetti kenname",
                        isSettled = false
                    )
                )

                dao.insertDebt(
                    DebtEntity(
                        bookId = defaultBookId,
                        contactName = "Kabbadaa Dajanee",
                        phoneNumber = "+251922334455",
                        type = "TO_PAY", // Narra jira
                        amount = 1800.0,
                        currency = "ETB",
                        dueDate = now + (oneDay * 3),
                        notes = "Kaffaltii mi'a suphaa",
                        isSettled = false
                    )
                )
            }
        }
    }
}
