package com.example.shopmaster.delete

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

public class RecyclerViewItemTouchHelper() :ItemTouchHelper.Callback(){

    private val rItemDragHelperCallback = RecyclerViewItemTouchHelper()
    private val rItemTouchHelper = ItemTouchHelper(rItemDragHelperCallback)

    private var recordAdapter:itemTouchListener? = null

    interface itemTouchListener{
        fun itemRemove(position:Int)
    }

    fun setItemTouchListener(itemTouchListener:itemTouchListener){
        this.recordAdapter = itemTouchListener
    }




    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var swipe = 0
        var move = 0
        swipe = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT or  ItemTouchHelper.START or ItemTouchHelper.END
        move = ItemTouchHelper.UP  or ItemTouchHelper.DOWN

        return ItemTouchHelper.Callback.makeMovementFlags(move, swipe)
    }

    override public fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override public fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        recordAdapter?.itemRemove()
    }

}