package com.example.shopmaster.add

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class AddBody(var pic: RequestBody, var item_name:RequestBody, var sort_id:RequestBody, var price:RequestBody, var stock: RequestBody) {
}



//class AddBody(var pic: MultipartBody.Part, var item_name:String, var sort_id:Int, var price:Int, var stock:Int) {
//}