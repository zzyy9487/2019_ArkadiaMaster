package com.example.shopmaster

import android.content.Context

class SharedPreferences(context: Context) {
    val preference = context.getSharedPreferences("Day1", Context.MODE_PRIVATE)

    fun setToken(token:String){
        preference.edit().putString("token", token).apply()
    }



}