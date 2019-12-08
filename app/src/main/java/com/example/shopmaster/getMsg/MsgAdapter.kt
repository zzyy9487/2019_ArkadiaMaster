package com.example.shopmaster.getMsg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.shopmaster.R
import kotlinx.android.synthetic.main.cell_layout_msg.view.*
import java.lang.Math.random

class MsgAdapter:RecyclerView.Adapter<MsgAdapter.ViewHolder>() {

    var whattype :Int = 0
    var whatname :String = ""
    private var inputList = listOf<AllMsgItem>()
    var userImageList = listOf(
        R.drawable.fireicon,
        R.drawable.watericon,
        R.drawable.grassicon)

    private var itemClickListener: clickedListener? = null

    interface clickedListener{
        fun sendMsgBack(id:Int, sheep_id:Int, sheep_msg:String, wolf_id:Int?, wolf_msg:String?)
    }

    fun setclickedListener(checkedListener: clickedListener){
        this.itemClickListener = checkedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_layout_msg, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return inputList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindViewHolder(inputList[position])

    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){

        val userName = itemView.textChatUserName
        val userImage = itemView.imageChatUser
        val userSay = itemView.textUserSay
        val masterName = itemView.textChatMasterName
        val masterimage = itemView.imageChatMaster
        val masterSay = itemView.textChatMasterSay

        fun bindViewHolder(allMsgItem: AllMsgItem){

            Glide.with(itemView).load(userImageList.random())
                .transform(CircleCrop())
                .into(userImage)
            userName.text = whatname
            userSay.text = allMsgItem.sheep_msg

            Glide.with(itemView).load(R.drawable.kaka)
                .transform(CircleCrop())
                .into(masterimage)

            masterSay.text = allMsgItem.wolf_msg?:"尚未回覆"
            if (masterSay.text == "尚未回覆"){
                Glide.with(itemView).load(R.drawable.wolf)
                    .transform(RoundedCorners(20))
                    .into(masterimage)
                masterName.text = "Wolf"
                itemView.setOnClickListener {
                    itemClickListener?.sendMsgBack(allMsgItem.id, allMsgItem.sheep_id, allMsgItem.sheep_msg, allMsgItem.wolf_id, allMsgItem.wolf_msg)
                }
            } else masterName.text = "Pikachu"

        }
    }

    fun update(newList: List<AllMsgItem>){
        inputList = newList
        notifyDataSetChanged()
    }

}
