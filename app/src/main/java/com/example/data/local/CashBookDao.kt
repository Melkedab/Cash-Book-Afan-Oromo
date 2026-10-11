package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashBookEntity
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashBookDao {

    // Transactions
    @Query("SELECT * FROM transactions WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getTransactionsByBook(bookId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE bookId = :bookId")
    suspend fun deleteTransactionsByBook(bookId: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    // Cash Books
    @Query("SELECT * FROM cash_books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<CashBookEntity>>

    @Query("SELECT * FROM cash_books WHERE id = :id")
    suspend fun getBookById(id: Long): CashBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: CashBookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<CashBookEntity>)

    @Update
    suspend fun updateBook(book: CashBookEntity)

    @Delete
    suspend fun deleteBook(book: CashBookEntity)

    // Debt Records
    @Query("SELECT * FROM debt_records WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getDebtsByBook(bookId: Long): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debt_records ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtEntity>)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debt_records WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Query("DELETE FROM debt_records")
    suspend fun clearAllDebts()
}
