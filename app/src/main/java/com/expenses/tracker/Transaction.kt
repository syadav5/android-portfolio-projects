package com.expenses.tracker

import java.math.BigDecimal
import java.util.*

enum class SpendingCategory { GROCERIES, EDUCATION, CLOTHING, BILLS, OTHERS }
interface ExpandableDataModel{
    var isExpanded:Boolean
}
data class Transaction (
    val desc: String,
    val dateOfPurchase: Date,
    val amount: BigDecimal,
    val id:Int = -1,
    val formattedDateOfPurchase:String = getFormattedDate(dateOfPurchase),
    val categories: SpendingCategory = SpendingCategory.OTHERS, override var isExpanded: Boolean = false
) : ExpandableDataModel

data class TransactionSummary(
    val summaryDate: Date,
    val summaryAmount: BigDecimal,
    var listOfTxns: ArrayList<Transaction> = arrayListOf(),
    override var isExpanded: Boolean = false
) : ExpandableDataModel {
    fun addChildren(transaction: Transaction) {
        listOfTxns.add(transaction)
    }
}