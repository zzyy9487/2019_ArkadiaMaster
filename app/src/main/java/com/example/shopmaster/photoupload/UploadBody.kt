package com.example.shopmaster.photoupload

import okhttp3.MultipartBody

class UploadBody(var item_id:Int, var pic: MultipartBody.Part) {
}