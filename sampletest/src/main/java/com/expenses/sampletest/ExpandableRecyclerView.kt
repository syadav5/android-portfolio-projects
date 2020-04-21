package com.expenses.sampletest

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class ExpandableRecyclerView constructor( context: Context, attrsSet:AttributeSet? = null, styleRes:Int = 0): RecyclerView(context,attrsSet,styleRes) {

}
interface ExpandCollapseViewListener{
    fun onExpand()
    fun onCollapse()
}
abstract class ExpandableModel<P,C>(){
   abstract val parent:P
   abstract val children:ArrayList<C>
    abstract val childreSize:Int
}
abstract class ExpandableRecyclerAdapter<P,C>(val dataList:List<ExpandableModel<P,C>>):RecyclerView.Adapter<ExpandableViewHolder>() {
    var expandCollapseListener : ExpandCollapseViewListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandableViewHolder {
        var vw = LayoutInflater.from(parent.context).inflate(R.layout.exach_expandable_parent_view,null)
        val parentView = LayoutInflater.from(parent.context).inflate(getParentLayoutIdRes(),null, false)
        vw.findViewById<LinearLayout>(R.id.parentViewContainer).addView(parentView)
        return ExpandableViewHolder(vw)
    }

    override fun getItemCount(): Int =
        this.dataList.size
    override fun onBindViewHolder(expandableViewHolder: ExpandableViewHolder, position: Int) {

        onBindParentViewHolder(expandableViewHolder.parentViewContainer, position)
        //Get Child View Container from this itemView and add Children View to it for each Child items displayed under this parent item based on the position.
        dataList.get(position)?.children.forEachIndexed { childPosition, any ->
            //for each item in the child list, create views and bind data to them and attach them to child view container.
            val child  = LayoutInflater.from(expandableViewHolder.itemView.context).inflate(getChildLayoutIdRes(),null,false)
            onBindChildViewHolder(child, childPosition , parentPosition = position)
            expandableViewHolder.childViewContainer.addView(child)
        } // By the end of this loop, childViewContainer should contain all the child views to be displayed.
        expandableViewHolder.childViewContainer.visibility = View.VISIBLE
    }
    @LayoutRes
    abstract fun getChildLayoutIdRes():Int
    @LayoutRes
    abstract fun getParentLayoutIdRes():Int
    abstract fun onBindParentViewHolder(parentContainerView:View, parentPosition:Int)
    abstract fun onBindChildViewHolder(childView:View,childPosition:Int,parentPosition:Int)
}

class ExpandableViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    //itemView here is the entire layout..
    val parentViewContainer = itemView.findViewById<LinearLayout>(R.id.parentViewContainer)
    val childViewContainer = itemView.findViewById<LinearLayout>(R.id.childViewContainer)

}