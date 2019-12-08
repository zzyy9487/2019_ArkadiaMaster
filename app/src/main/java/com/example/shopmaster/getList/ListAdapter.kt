package com.example.shopmaster.getList

import android.graphics.NinePatch
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.shopmaster.R
import kotlinx.android.synthetic.main.cell_layout.view.*

class ListAdapter:RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private var inputList = listOf<CellItem>()
    private var itemClickListener: clickedListener? = null

    private val rItemDragHelperCallback = RecyclerViewItemTouchHelper()
    private val rItemTouchHelper = ItemTouchHelper(rItemDragHelperCallback)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        rItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    interface clickedListener{
        fun modifyItemData(id:Int, item_name:String, sort_id:Int, sort_name:String, price:Int, stock:Int ,pic:String)
        fun removeItemData(id:Int)
    }

    fun setClickListener(checkedListener: clickedListener){
        this.itemClickListener = checkedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return inputList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindViewHolder(inputList[position])

    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

        val pic = itemView.imagePic
        val name = itemView.textName
        val price = itemView.textPrice
        val stock = itemView.textStock
        val sort = itemView.textSort

        fun bindViewHolder(cellitem: CellItem){

            Glide.with(itemView).load(cellitem.pic)
                .transform(RoundedCorners(10))
                .into(pic)
            name.text = cellitem.item_name
            price.text = cellitem.price.toString()
            stock.text = cellitem.stock.toString()
            sort.text = cellitem.sort_name

            itemView.setOnClickListener {
                itemClickListener?.modifyItemData(cellitem.id, cellitem.item_name, cellitem.sort_id, cellitem.sort_name, cellitem.price, cellitem.stock?:0, cellitem.pic?:"")
            }


        }
    }


    inner class RecyclerViewItemTouchHelper : ItemTouchHelper.Callback(){

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            var swipe = 0
            var move = 0
            swipe = ItemTouchHelper.START or ItemTouchHelper.END
            move = ItemTouchHelper.UP  or ItemTouchHelper.DOWN

            return makeMovementFlags(move, swipe)
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            itemClickListener?.removeItemData(inputList[position].id)
        }
    }


    fun update(newList: List<CellItem>){
//        val res = DiffUtil.calculateDiff(object :DiffUtil.Callback(){
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return inputList[oldItemPosition].id == newList[newItemPosition].id
//            }
//
//            override fun getOldListSize(): Int {
//                return inputList.size
//            }
//
//            override fun getNewListSize(): Int {
//                return newList.size
//            }
//
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return inputList[oldItemPosition] == newList[newItemPosition]
//            }
//
//        })
        inputList = newList
//        res.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

}
