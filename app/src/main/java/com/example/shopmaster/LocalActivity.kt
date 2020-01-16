package com.example.shopmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.shopmaster.record.RecordData
import kotlinx.android.synthetic.main.activity_local.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class LocalActivity : AppCompatActivity() {

    lateinit var timer: Timer
    lateinit var shared :SharedPreferences
    var token :String = ""
    var userlv:String = ""
    var userstock:String = ""
    var usertotal:String = ""
    var score :Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local)

        shared = SharedPreferences(this)

        if (!shared.preference.getString("token", "").isNullOrEmpty()){
            token = "Bearer "+ shared.preference.getString("token", "")
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://35.229.181.103")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiInterface = retrofit.create(APIInterface::class.java)
        val call = apiInterface.record(token)
        call.enqueue(object :retrofit2.Callback<RecordData>{
            override fun onFailure(call: Call<RecordData>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call<RecordData>, response: Response<RecordData>) {
               if (response.code() == 200){
                   val data = response.body()
                   userlv = data!!.lv.toString()
                   userstock = data.all_stock.toString()
                   usertotal = data.all_total.toString()
                   score = data.last_lv
                   textLevel.text = userlv
                   textStock.text = userstock
                   textTotal.text = usertotal

                   when (userlv){
                       "1" -> imageHome.setImageResource(R.drawable.home0)
                       "2" -> imageHome.setImageResource(R.drawable.home1)
                       "3" -> imageHome.setImageResource(R.drawable.home2)
                       "4" -> imageHome.setImageResource(R.drawable.home3)
                       "5" -> imageHome.setImageResource(R.drawable.home4)
                   }

                   if (userlv == "5"){
                       progressBar.max = usertotal.toInt()
                       progressBar.setProgress(usertotal.toInt(), true)
                   }
                   else{
                       var usertotall = usertotal.toInt() + score
                       progressBar.max = usertotall
                       progressBar.setProgress(usertotal.toInt(), true)
                   }



               }
            }
        })






        timer = Timer(true)
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://35.234.60.173")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface = retrofit.create(APIInterface::class.java)
                val call = apiInterface.record(token)
                call.enqueue(object :retrofit2.Callback<RecordData>{
                    override fun onFailure(call: Call<RecordData>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                    override fun onResponse(call: Call<RecordData>, response: Response<RecordData>) {
                        if (response.code() == 200){
                            val data = response.body()
                            userlv = data!!.lv.toString()
                            userstock = data.all_stock.toString()
                            usertotal = data.all_total.toString()
                            score = data.last_lv

                            this@LocalActivity.runOnUiThread{
                                textLevel.text = userlv
                                textStock.text = userstock
                                textTotal.text = usertotal
                                when (userlv){
                                    "1" -> imageHome.setImageResource(R.drawable.home0)
                                    "2" -> imageHome.setImageResource(R.drawable.home1)
                                    "3" -> imageHome.setImageResource(R.drawable.home2)
                                    "4" -> imageHome.setImageResource(R.drawable.home3)
                                    "5" -> imageHome.setImageResource(R.drawable.home4)
                                }

                                if (userlv == "5"){
                                    progressBar.max = usertotal.toInt()
                                    progressBar.setProgress(usertotal.toInt(), true)
                                }
                                else{
                                    var usertotall = usertotal.toInt() + score
                                    progressBar.max = usertotall
                                    progressBar.setProgress(usertotal.toInt(), true)
                                }
                            }
                        }
                    }
                })
            }
        }
        timer.schedule(timerTask, 10000, 10000)


        btn_item.setOnClickListener {
            val intent = Intent(this@LocalActivity, ItemActivity::class.java)
            startActivity(intent)
        }

        btn_msg.setOnClickListener {
            val intent = Intent(this@LocalActivity, MsgActivity::class.java)
            startActivity(intent)
        }
    }
}
