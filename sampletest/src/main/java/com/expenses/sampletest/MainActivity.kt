package com.expenses.sampletest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val data : ArrayList<TransactionData> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initList()
        val adapter = MyAdapter(data)
        expandableRv.adapter = adapter
        expandableRv.layoutManager = LinearLayoutManager(this)
        expandableRv.setHasFixedSize(true)

    }

    private fun initList() {
    val listOfTxs = ArrayList<TransactionData>()
        val l1 = arrayListOf<TransactionEntry>(
            TransactionEntry("LAPTOP","444"), TransactionEntry("TABLET","344")
        )
        listOfTxs.add(TransactionData("24-AUG-13","34",l1))
        val l2 = arrayListOf<TransactionEntry>(
            TransactionEntry("GROCERIES","23"), TransactionEntry("PHONE","44")
        )
        listOfTxs.add(TransactionData("22-MAY-17","304",l1))
        data.clear()
        data.addAll(listOfTxs)
    }
}
class TransactionData(val txSummary:String, val totalAmount: String, override val children: ArrayList<TransactionEntry> = arrayListOf()) : ExpandableModel<TransactionSummary,TransactionEntry>(){
    override val parent: TransactionSummary
        get() = TransactionSummary(txSummary,totalAmount)

    fun addEntry(item:String, cost:String) {
        this.children.add((TransactionEntry(item,cost)))
    }

    override val childreSize: Int
        get() = children.size
}

data class TransactionEntry(val dataItem:String, val cost:String)
data class TransactionSummary(val dat:String, val totalAmount:String)
class MyAdapter(dataList:ArrayList<TransactionData>) : ExpandableRecyclerAdapter<TransactionSummary,TransactionEntry>(dataList){
    override fun getChildLayoutIdRes(): Int {
        return R.layout.child_detail_layout
    }

    override fun getParentLayoutIdRes(): Int {
        return R.layout.parent_dop_layout
    }

    override fun onBindParentViewHolder(parentContainerView: View, parentPosition: Int) {
      /*  dataList.get(parentPosition).let {

            parentContainerView.findViewById<TextView>(R.id.dop_parent).text= it.parent.dat
            parentContainerView.findViewById<TextView>(R.id.dop_parent).text= it.parent.dat

        }*/
        parentContainerView.findViewById<TextView>(R.id.dop_parent).text= "SAMPLE ${parentPosition}"
        parentContainerView.findViewById<TextView>(R.id.amountTotal).text= "SAMPLE AMOUNT ${parentPosition}"
    }
    override fun onBindChildViewHolder(childView: View, childPosition: Int, parentPosition: Int) {
        childView.findViewById<TextView>(R.id.itemName).text= "SAMPLE ITEM NAME ${childPosition} FOR ${parentPosition}"
        childView.findViewById<TextView>(R.id.itemAmount).text= "SAMPLE ITEM AMOUNT ${childPosition} FOR ${parentPosition}"
    }
}