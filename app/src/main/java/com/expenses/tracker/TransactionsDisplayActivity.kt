package com.expenses.tracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_transaction_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class TransactionsDisplayActivity : AppCompatActivity() {
    private var lastDeletedItem: Transaction? = null
    private var lastDeletedItemPosition:Int = -1
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    val dbCon = DatabaseHelper(this)
    val listOfTransaction: ArrayList<Transaction> = arrayListOf()
val listOfTransactionsWithSummary:ArrayList<TransactionSummary> = arrayListOf()
    val mapOfTxnsGroupedByDate: Map<String,Pair<BigDecimal,List<Transaction>>> = mapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setup recycler view
        txlist_rv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransactionAdapter(
                listOfTransactionsWithSummary , object: OnChildItemItemDeleted {
                    override fun deleteChildItem(position: Int) {
                        dbCon.deleteTransactionById(position)
                        getTransactions(dbCon)
                    }
                }
            )
            setHasFixedSize(true)
            var itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            //itemDecoration.setDrawable(getDrawable(R.drawable.border2)!!)
            addItemDecoration(itemDecoration)
        }
      //  ItemTouchHelper(getSwipeToDismissCallback()).attachToRecyclerView(txlist_rv)
        //setup Bottom Sheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        getTransactions(DatabaseHelper(this))
        addBtn.setOnClickListener {
            dbCon.createNewTransaction(
                itemNameEt.text.toString(),
                dateEt.text.toString(),
                amountSpentEt.text.toString()
            )
            hideBottomSheet()
            getTransactions(dbCon)
        }
        initChart()
        initListeners()
    }

    private fun getSwipeToDismissCallback(): Callback {
        return object : SimpleCallback(0, RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == RIGHT) {
                    Log.d("REMOVED!! ${viewHolder.adapterPosition}", "${listOfTransactionsWithSummary.get(viewHolder.adapterPosition)}")
                   // Log.d("REMOVED!!", "${listOfTransactionsWithSummary.get(viewHolder.adapterPosition).listOfTxns.get(viewHolder.adapterPosition)}")

                    /* lastDeletedItem = listOfTransaction.get(viewHolder.adapterPosition)
                    lastDeletedItemPosition = viewHolder.adapterPosition*/
                    //deleteItemFromDatabase()
                    //txlist_rv.adapter?.notifyDataSetChanged()
                   // createSnackbar()
                }
            }
        }
    }

    private fun initListeners() {
        addTxnFab.setOnClickListener {
            showBottomSheet()
        }
        dateEt.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("Select Date")
            builder.build().show(supportFragmentManager, "Expenditue_Dt")
        }
        dateEt.setOnFocusChangeListener { view, isFocused ->
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("Select Date")
            val materialDatePicker = builder.build()
            materialDatePicker.addOnPositiveButtonClickListener { selection ->
                // selection is date in milliseconds based on UTC TimeZone
                dateEt.setText(
                    getFormattedDateWithDaylightSavings(
                        selection,
                        timeZone = TimeZone.getTimeZone("UTC")
                    )
                )
            }
            materialDatePicker.addOnCancelListener {
            }
            materialDatePicker.addOnDismissListener {
                dateEt.clearFocus()
            }
            if (isFocused) {
                materialDatePicker.show(supportFragmentManager, "Expenditure_Dt")
            } else {
                if (materialDatePicker.isVisible)
                    materialDatePicker.dismiss()
            }
        }
    }

    private fun initChart() {
        var barChart = barChartView
        var barEntries: ArrayList<BarEntry> = arrayListOf()
        val calendar = Calendar.getInstance()
        /*  listOfTransaction.forEach { tx ->
              calendar.time = tx.dateOfPurchase

  //            barEntries.add(BarEntry(Math.random(1,7).toFloat(), tx.amount.toFloat()))
          }*/
        barEntries.add(BarEntry(1f, 100f))
        barEntries.add(BarEntry(2f, 150f))
        barEntries.add(BarEntry(3f, 0f))
        barEntries.add(BarEntry(4f, 0f))
        barEntries.add(BarEntry(5f, 500f))
        barEntries.add(BarEntry(6f, 10f))
        barEntries.add(BarEntry(7f, 30f))
        val labelsArr: ArrayList<String> = ArrayList()
        val obj = object : ValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                if (value >= 0 && value < labelsArr.size) {
                    return labelsArr.get(value.toInt())
                }
                return ""
            }
        }

        val barDataSet = BarDataSet(barEntries, "Expenses")
        labelsArr.add("Monday")
        labelsArr.add("Tuesday")
        labelsArr.add("Wednesday")
        labelsArr.add("Thursday")
        labelsArr.add("Friday")
        labelsArr.add("Saturday")
        labelsArr.add("Sunday")
        val barData = BarData(barDataSet)
        /*   barData.dataSetLabels= arrayOf<String>(labelsArr.toArray())*/
        barChartView.xAxis.valueFormatter = obj
        barChartView.data = barData
        barChartView.description = Description()
        barChartView.description.text = "Expenditure - Last Week"
        barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        barChartView.animateY(5000)
        barChart.setFitBars(true)
        barChartView.invalidate()
        barChartView.setOnClickListener {
            Toast.makeText(this, "GOING TO GET DATA ", Toast.LENGTH_LONG).show()
            //getTransactionsByDate()
        }
    }
    private fun getTransactions(dbCon: DatabaseHelper) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("KRIS", "RUNNING ON THREAD ${Thread.currentThread().name}")
            val txns = dbCon.getAllTxByDate()
            listOfTransactionsWithSummary.clear()
            txns.forEach { (_, value) ->
                listOfTransactionsWithSummary.add(value)
            }
            ///End
            withContext(Dispatchers.Main) {
                Log.d("KRIS", "RUNNING ON THREAD ${Thread.currentThread().name}")
                Log.d("KRIS->IN MAIN LIST", listOfTransactionsWithSummary.joinToString())
                if (listOfTransactionsWithSummary.isEmpty()) {
                    txlist_rv.visibility = View.GONE
                    no_data_found.visibility = View.VISIBLE
                } else {
                    txlist_rv.visibility = View.VISIBLE
                    no_data_found.visibility = View.GONE
                }
                (txlist_rv.adapter as TransactionAdapter).notifyDataSetChanged()
                txlist_rv.scrollToPosition(listOfTransaction.size - 1)
                hideBottomSheet()
            }
        }
    }
/*
    private fun getTransactions(dbCon: DatabaseHelper) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("KRIS", "RUNNING ON THREAD ${Thread.currentThread().name}")
            listOfTransaction.clear()
            listOfTransaction.addAll(dbCon.getAllTransactions())
            Log.d("KRIS->ALL LIST", listOfTransaction.joinToString())
            val mapOfpurchaseDtToorders= listOfTransaction.groupBy {
                it.dateOfPurchase
            }
            for((ky,vl) in mapOfpurchaseDtToorders) {
                println(" *************** PURCHASE DT ${ky} AND ALL TXNS = ${vl.joinToString(",")}")
            }
            withContext(Dispatchers.Main) {
                Log.d("KRIS", "RUNNING ON THREAD ${Thread.currentThread().name}")
                Log.d("KRIS->IN MAIN LIST", listOfTransaction.joinToString())
                (txlist_rv.adapter as TransactionAdapter).notifyDataSetChanged()
                txlist_rv.scrollToPosition(listOfTransaction.size - 1)
                hideBottomSheet()
            }
        }
    }*/

    private fun hideBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun displayToast(checkedChiStringpId: String) {
        Toast.makeText(this, "CHECKED CHIP ID: ${checkedChiStringpId}", Toast.LENGTH_LONG).show()
    }

    fun showBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            // addTxnFab.visibility = View.GONE
        }
    }

    fun createSnackbar() {
     val snackbar =   Snackbar.make(coordinator, "Transaction deleted.", Snackbar.LENGTH_LONG)
            .setAction("UNDO" , View.OnClickListener {
                lastDeletedItem?.let{
                    listOfTransaction.add(lastDeletedItemPosition,it)
                    txlist_rv.adapter?.notifyDataSetChanged()
                    lastDeletedItem = null
                    lastDeletedItemPosition = -1
                }
            })

        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                deleteItemFromDatabase()
                lastDeletedItem = null
                lastDeletedItemPosition = -1
            }
        })
        snackbar.show()
    }

    private fun deleteItemFromDatabase() {
        lastDeletedItem?.let {
            CoroutineScope(Dispatchers.IO).launch {
                DatabaseHelper(applicationContext).deleteTransactionById(
                    it.id
                )
            }
        }
    }
}
/*
class TransactionAdapter(val listOfTxs: List<Transaction>) :
    RecyclerView.Adapter<TransactionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.each_tx_row, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfTxs.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.amountTv.text = listOfTxs.get(position).amount.toPlainString()
        holder.descTv.text = listOfTxs.get(position).desc
        holder.dt.text = listOfTxs.get(position).formattedDateOfPurchase
    }

}

class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val descTv = view.findViewById<TextView>(R.id.desc_tv)
    val amountTv = view.findViewById<TextView>(R.id.amount_tv)
    val dt = view.findViewById<TextView>(R.id.dt_tv)
}
*/
