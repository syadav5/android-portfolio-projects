package com.expenses.tracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

fun getFormattedDate(date: Date, format: String = "yyyy-MM-dd", timeZone: TimeZone? = TimeZone.getDefault()): String {
    val sdf = SimpleDateFormat(format)
    timeZone?.let {
        sdf.timeZone = timeZone
    }
    return sdf.format(date)
}
fun getFormattedDateWithDaylightSavings(time: Long, format: String = "yyyy-MM-dd", timeZone: TimeZone = TimeZone.getDefault()): String {
    val sdf = SimpleDateFormat(format)
     sdf.timeZone = timeZone
    return sdf.format(time)
}

fun getParsedDate(date: String, format: String = "yyyy-MM-dd"): Date {
    try {
        val sdf = SimpleDateFormat(format)
        return sdf.parse(date)
    } catch (ex: Exception) {
        ex.printStackTrace()
        return Date()
    }
}

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        val DB_VERSION = 1
        val DB_NAME = "EXPENSE_TRACKER_DATABASE.DB"
        val TRANSACTION_TABLE_NAME = "transactions"
        val TRANSACTIONS_COLUMN_ID = "tx_id"
        val TRANSACTIONS_COLUMN_ITEM_NAME = "item_name"
        val TRANSACTIONS_COLUMN_AMOUNT = "amount"
        val TRANSACTIONS_COLUMN_DATE_PURCHASED = "tx_date"
        val GET_ALL_TRANSACTIONS = "select * from ${TRANSACTION_TABLE_NAME}"
        val GET_ALL_TRANSACTIONS_ORDER_BY_DT_DESC = "select * from ${TRANSACTION_TABLE_NAME} order by tx_date desc"
        val GET_ALL_TRANSACTIONS_GROUPED_BY_DT =
            "select sum(amount) as total, ${TRANSACTIONS_COLUMN_ID}, ${TRANSACTIONS_COLUMN_DATE_PURCHASED}, ${TRANSACTIONS_COLUMN_ITEM_NAME}, ${TRANSACTIONS_COLUMN_AMOUNT} from ${TRANSACTION_TABLE_NAME} group by ${TRANSACTIONS_COLUMN_DATE_PURCHASED} order by tx_date desc"

        val GET_SUMMARY_AND_TRANSACTIONS_ORDER_BY_DATE =
            "select 1 as SUMMARY, sum(amount) as total, null as tx_id, tx_date, null as item_name, null as amount from transactions" +
                    " group by tx_date " +
                    " UNION " +
                    " select 0 AS SUMMARY, null as total, tx_id, tx_date, item_name, amount from transactions " +
                    " ORDER BY tx_date  DESC, amount ASC "
        val CREATE_DB_QUERY =
            "create table ${TRANSACTION_TABLE_NAME} (${TRANSACTIONS_COLUMN_ID} integer primary key, ${TRANSACTIONS_COLUMN_ITEM_NAME} text, ${TRANSACTIONS_COLUMN_AMOUNT} NUMERIC, ${TRANSACTIONS_COLUMN_DATE_PURCHASED} text)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_DB_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${TRANSACTION_TABLE_NAME}")
        onCreate(db)
    }

    fun createNewTransaction(item: String, dateOfPurchase: String, amount: String): Boolean {
        var wDb = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TRANSACTIONS_COLUMN_ITEM_NAME, item)
        contentValues.put(TRANSACTIONS_COLUMN_AMOUNT, amount)
        contentValues.put(TRANSACTIONS_COLUMN_DATE_PURCHASED, dateOfPurchase)
        val id = wDb.insert(TRANSACTION_TABLE_NAME, null, contentValues)
        return id > 0
    }

    fun deleteTransactionById(id: Int): Boolean {
        var wDb = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TRANSACTIONS_COLUMN_ID, id)
        val id = wDb.delete(TRANSACTION_TABLE_NAME, "${TRANSACTIONS_COLUMN_ID} = ?", arrayOf(id.toString()))
        return id > 0
    }

    fun getAllTransactions(): List<Transaction> {
        val listOfTransactions = arrayListOf<Transaction>()
        var rDb = readableDatabase
        val cursor = rDb.rawQuery(GET_ALL_TRANSACTIONS_ORDER_BY_DT_DESC, null)
        Log.d("KRIS", "CURSOR COUNT IS ${cursor.count}")
        if (cursor.moveToFirst()) {
            do {
                var item = cursor.getString(cursor.getColumnIndex(TRANSACTIONS_COLUMN_ITEM_NAME))
                var id = cursor.getInt(cursor.getColumnIndex(TRANSACTIONS_COLUMN_ID))
                var dt = cursor.getString(cursor.getColumnIndex(TRANSACTIONS_COLUMN_DATE_PURCHASED))
                var amount = cursor.getDouble(cursor.getColumnIndex(TRANSACTIONS_COLUMN_AMOUNT))
                val formattedDt = getParsedDate(dt)
                val tx = Transaction(item, formattedDt, BigDecimal.valueOf(amount), id)
                listOfTransactions.add(tx)
            } while (cursor.moveToNext())
            cursor.close()
        }
        Log.d("DB HELPER", listOfTransactions.joinToString(","))
        return listOfTransactions
    }

    fun getAllTxByDate(): Map<String,TransactionSummary> {
        val listOfTransactions = arrayListOf<Pair<BigDecimal, String>>()
        var mapTxns = mutableMapOf<String, TransactionSummary>()
        var rDb = readableDatabase
        val cursor = rDb.rawQuery(GET_SUMMARY_AND_TRANSACTIONS_ORDER_BY_DATE, null)
        Log.d("KRIS", "CURSOR COUNT IS ${cursor.count}")
        if (cursor.moveToFirst()) {
            do {
                var isSummary = cursor.getInt(cursor.getColumnIndex("SUMMARY")) > 0
                var total = cursor.getDouble(1)
                var txId = cursor.getInt(cursor.getColumnIndex(TRANSACTIONS_COLUMN_ID))
                var dateOfPurchase =
                    cursor.getString(cursor.getColumnIndex(TRANSACTIONS_COLUMN_DATE_PURCHASED))
                var itemName =
                    cursor.getString(cursor.getColumnIndex(TRANSACTIONS_COLUMN_ITEM_NAME))
                var amount = cursor.getDouble(cursor.getColumnIndex(TRANSACTIONS_COLUMN_AMOUNT))
               // val formattedDt = getParsedDate(dateOfPurchase)
                if (isSummary) {
                    val txSummary = TransactionSummary(
                        getParsedDate(dateOfPurchase), BigDecimal.valueOf(total),
                        arrayListOf()
                        //Transaction(itemName,getParsedDate(dateOfPurchase),BigDecimal.valueOf(amount),txId))
                    )
                    mapTxns.put(dateOfPurchase, txSummary)
                }
                else {
                    val txSummary = mapTxns.get(dateOfPurchase)
                    txSummary!!.addChildren(
                        Transaction(
                            itemName,
                            getParsedDate(dateOfPurchase),
                            BigDecimal.valueOf(amount),
                            txId
                        )
                    )
                    mapTxns.put(dateOfPurchase,txSummary)
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
        Log.d("DB HELPER", listOfTransactions.joinToString(","))
        return mapTxns
    }
}