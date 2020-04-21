package com.expenses.tracker

import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.math.BigDecimal

class TransactionAdapter(
    private val listOfTotalTransactions: List<TransactionSummary>,
    val onChildItemItemDeleted: OnChildItemItemDeleted
) :
    RecyclerView.Adapter<TransactionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        var id = R.layout.parent_layout
        var view =
            LayoutInflater.from(parent.context).inflate(id, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfTotalTransactions.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        var txSummary = listOfTotalTransactions.get(position)
        holder.summaryDate.text = txSummary.summaryDate.toString()
        holder.summaryAmount.text = txSummary.summaryAmount.toPlainString()
        holder.childRv.visibility = if(txSummary.isExpanded) View.VISIBLE else View.GONE
        holder.populateChildren(txSummary.listOfTxns, object : OnChildItemItemDeleted {
            override fun deleteChildItem(position: Int) {
                val itemToDelete = txSummary.listOfTxns.get(position)
                //txSummary.listOfTxns.removeAt(position)
                onChildItemItemDeleted.deleteChildItem(itemToDelete.id)
                txSummary.summaryAmount.minus(itemToDelete.amount)
                holder.itemView.visibility = if(txSummary.summaryAmount > BigDecimal.ZERO) View.VISIBLE else View.GONE
                notifyDataSetChanged()
            }
        })
        holder.itemView.setOnClickListener {
            txSummary.isExpanded = !txSummary.isExpanded
            holder.toggleChildrenVisibility(txSummary.isExpanded)
        }
    }
}
interface OnChildItemItemDeleted{
    fun deleteChildItem(position:Int)
}
class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val summaryDate = view.findViewById<TextView>(R.id.summarydateOfExpenditure)
    val summaryAmount = view.findViewById<TextView>(R.id.summaryExpenditureAmount)
    val childRv = view.findViewById<RecyclerView>(R.id.transactionsDetailsRv)
    fun toggleChildrenVisibility(isExpanded: Boolean) {
        childRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    fun populateChildren(
        txDetails: List<Transaction>,
        onChildItemItemDeleted: OnChildItemItemDeleted
    ) {
        val txDetailsAdapter = TransactionDetailsAdapter(txDetails, onChildItemItemDeleted)
        childRv.apply {
            layoutManager = LinearLayoutManager(this.context)
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
            adapter = txDetailsAdapter
            var itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            addItemDecoration(itemDecoration)
            //ItemTouchHelper(getChildItemsTouchHelper(txDetailsAdapter, parentPosition)).attachToRecyclerView(childRv)
        }
    }
}
class TransactionDetailsAdapter(
    val data: List<Transaction>,
    val onChildItemItemDeleted: OnChildItemItemDeleted
) :
    RecyclerView.Adapter<TransactionDetailsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionDetailsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.transaction_details_layout, null)
        return TransactionDetailsViewHolder(view)
    }

    override fun getItemCount(): Int  = data.size


    override fun onBindViewHolder(holder: TransactionDetailsViewHolder, position: Int) {
        val txDetail = data.get(position)
        holder.itemName.text = txDetail.desc
        holder.itemCost.text = txDetail.amount.toPlainString()
        holder.delIcon.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    onChildItemItemDeleted.deleteChildItem(position)
                    notifyDataSetChanged()
                })
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                .create().show()
        }
    }
}

class TransactionDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemName: TextView = itemView.findViewById(R.id.itemPurchasedTv)
    val itemCost: TextView = itemView.findViewById(R.id.itemCostTv)
    val delIcon:ImageView = itemView.findViewById(R.id.deleteicon)
}
