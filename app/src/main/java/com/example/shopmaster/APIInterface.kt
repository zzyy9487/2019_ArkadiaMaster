package com.example.shopmaster

import com.example.shopmaster.add.AddBody
import com.example.shopmaster.add.AddData
import com.example.shopmaster.delete.DeleteData
import com.example.shopmaster.getList.ListData
import com.example.shopmaster.getMsg.GetMsgData
import com.example.shopmaster.login.LoginBody
import com.example.shopmaster.login.LoginData
import com.example.shopmaster.modify.ModifyData
import com.example.shopmaster.photoupload.UploadBody
import com.example.shopmaster.photoupload.UploadData
import com.example.shopmaster.record.RecordData
import com.example.shopmaster.sendMsg.SendMsgBody
import com.example.shopmaster.sendMsg.SendMsgData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*

interface APIInterface {


    @Headers("Content-Type: application/json","Accept: application/json")

    @POST("/api/wolf/login")
    fun login(
        @Body body: LoginBody
    ): Call<LoginData>

    @GET("/api/items")
    fun getList(

    ):Call<ListData>

    @DELETE("/api/wolf/items/{item_id}")
    fun delete(
        @Path("item_id") item_id:String,
        @Header("Authorization") token:String
    ):Call<DeleteData>


    @Multipart
    @POST("/api/wolf/items")
    fun add(
        @Part item_name:MultipartBody.Part,
        @Part sort_id: MultipartBody.Part,
        @Part price: MultipartBody.Part,
        @Part stock: MultipartBody.Part,
        @Part file:MultipartBody.Part,
        @Header("Authorization") token:String
    ): Call<AddData>

    @FormUrlEncoded
    @PUT("/api/wolf/items/{item_id}")
    fun modify(
        @Path("item_id") item_id:String,
        @Header("Authorization") token:String,
        @Field("item_name") item_name:String?,
        @Field("sort_id") sort_id:Int?,
        @Field("price") price:Int?,
        @Field("stock") stock:Int?,
        @Field("pic") pic:String?
    ): Call<ModifyData>

    @GET("/api/wolfitem")
    fun record(
        @Header("Authorization") token:String
    ):Call<RecordData>

//    @POST("/api/wolf/items/photo_upload")
//    fun upload(
//        @Header("Authorization") token:String,
//        @Body body:UploadBody
//    ):Call<UploadData>

    @GET("/api/allmsg")
    fun getMsg(
        @Header("Authorization") token:String
    ):Call<GetMsgData>

    @PUT("/api/wolfreplay")
    fun sendMsg(
        @Header("Authorization") token:String,
        @Body body:SendMsgBody
    ):Call<SendMsgData>

}
