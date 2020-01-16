package com.example.shopmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.shopmaster.login.LoginBody
import com.example.shopmaster.login.LoginData
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    val name:String = "Arcadia"
    val account:String = "bala"
    val password:String = "00000000"
    lateinit var body: LoginBody
    lateinit var shared :SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shared = SharedPreferences(this)

        editResname.setText(name)
        editResaccount.setText(account)
        editRespass.setText(password)

        btn_login.setOnClickListener {
            body = LoginBody(editResaccount.text.toString(), editRespass.text.toString())
            val retrofit = Retrofit.Builder()
                .baseUrl("http://35.229.181.103")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val apiInterface = retrofit.create(APIInterface::class.java)
            val call = apiInterface.login(body)
            call.enqueue(object :Callback<LoginData>{
                override fun onFailure(call: Call<LoginData>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<LoginData>, response: Response<LoginData>) {
                    if (response.isSuccessful){
                        if (response.code() == 200){
                            val data = response.body()
                            shared.setToken(data!!.now_flower.api_token)
                            Toast.makeText(this@MainActivity, data!!.msg, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, LocalActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            })


        }

        btn_clear.setOnClickListener {
            editResname.setText("")
            editResaccount.setText("")
            editRespass.setText("")
        }

    }
}
