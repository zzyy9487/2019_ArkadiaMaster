package com.example.shopmaster

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopmaster.getMsg.AllMsgItem
import com.example.shopmaster.getMsg.GetMsgData
import com.example.shopmaster.getMsg.MsgAdapter
import com.example.shopmaster.sendMsg.SendMsgBody
import com.example.shopmaster.sendMsg.SendMsgData
import kotlinx.android.synthetic.main.activity_msg.*
import kotlinx.android.synthetic.main.activity_work.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MsgActivity : AppCompatActivity() {

    private lateinit var apiInterface: APIInterface
    lateinit var adapter: MsgAdapter
    lateinit var shared :SharedPreferences
    lateinit var body: SendMsgBody
    var token :String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_msg)
        shared = SharedPreferences(this)
        if (!shared.preference.getString("token", "").isNullOrEmpty()){
            token = "Bearer "+ shared.preference.getString("token", "")
        }

        adapter = MsgAdapter()
        recyclerMsg.layoutManager = LinearLayoutManager(this)
        recyclerMsg.adapter = adapter

        val retrofit = Retrofit.Builder()
            .baseUrl("http://35.229.181.103")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiInterface = retrofit.create(APIInterface::class.java)

        getMsg()

        adapter.setclickedListener(object :MsgAdapter.clickedListener{
            override fun sendMsgBack(id: Int, sheep_id: Int, sheep_msg: String, wolf_id: Int?, wolf_msg: String?) {
                val msgInflater = this@MsgActivity.layoutInflater
                val view = msgInflater.inflate(R.layout.alert_layout_msg, null)
                val builder = AlertDialog.Builder(this@MsgActivity)
                                            .setView(view)
                                            .show()
                val textusersaysomething = view.findViewById<TextView>(R.id.textusersaysomething)
                val edit = view.findViewById<EditText>(R.id.editMsg)
                val send = view.findViewById<TextView>(R.id.textMsgSend)
                val cancel = view.findViewById<TextView>(R.id.textMsgCancel)
                textusersaysomething.text = sheep_msg

                send.setOnClickListener {
                    if (edit.text.isEmpty()){
                        Toast.makeText(this@MsgActivity, "也太懶了吧...", Toast.LENGTH_SHORT).show()
                    } else {
                        body = SendMsgBody(id, edit.text.toString())
                        apiInterface.sendMsg(token, body).enqueue(object :Callback<SendMsgData>{
                            override fun onFailure(call: Call<SendMsgData>, t: Throwable) {
                                Toast.makeText(this@MsgActivity, "Unable to fetch data from API",Toast.LENGTH_SHORT).show()
                            }

                            override fun onResponse(call: Call<SendMsgData>, response: Response<SendMsgData>) {
                                if (response.isSuccessful){
                                    val data = response.body()
                                    Toast.makeText(this@MsgActivity, data!!.replay_msg.wolf_msg + "已成功回覆！！！",Toast.LENGTH_SHORT).show()
                                    getMsg()
                                }

                            }
                        })
                        builder.dismiss()
                    }

                }

                cancel.setOnClickListener {
                    builder.dismiss()
                }

            }
        })


    }

    private fun getMsg() {
        apiInterface.getMsg(token).enqueue(object : Callback<GetMsgData> {
            override fun onFailure(call: Call<GetMsgData>, t: Throwable) {
                Toast.makeText(
                    this@MsgActivity,
                    "Unable to fetch data from API",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call<GetMsgData>, response: Response<GetMsgData>) {
                if (response.isSuccessful) {
                    val inputList = response.body()!!.allmsg
                        .map {
                            AllMsgItem(
                                it.id,
                                it.sheep_id,
                                it.sheep_msg,
                                it.wolf_id,
                                it.wolf_msg,
                                it.created_at,
                                it.updated_at,
                                it.name,
                                it.type
                            )
                        }
                        .sortedBy { it.id }
                        .reversed()
                    adapter.update(inputList)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}
