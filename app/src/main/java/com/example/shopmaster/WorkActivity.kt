package com.example.shopmaster

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.shopmaster.add.AddBody
import com.example.shopmaster.add.AddData
import com.example.shopmaster.delete.DeleteData
import com.example.shopmaster.getList.CellItem
import com.example.shopmaster.getList.ListAdapter
import com.example.shopmaster.getList.ListData
import com.example.shopmaster.modify.ModifyData
import com.example.shopmaster.photoupload.UploadBody
import kotlinx.android.synthetic.main.activity_work.*
import kotlinx.android.synthetic.main.alert_layout2.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class WorkActivity : AppCompatActivity() {

    private lateinit var alertView: View
    private lateinit var apiInterface: APIInterface
    lateinit var adapter:ListAdapter
    lateinit var shared :SharedPreferences
    var bitmap:ByteArray? = null
    var list = mutableListOf<CellItem>()
    var token :String =""
    var array = listOf("糧食", "軍事", "特殊", "隱藏組合")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        shared = SharedPreferences(this)

        if (!shared.preference.getString("token", "").isNullOrEmpty()){
            token = "Bearer "+ shared.preference.getString("token", "")
        }

        adapter = ListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val retrofit = Retrofit.Builder()
            .baseUrl("http://35.234.60.173")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiInterface = retrofit.create(APIInterface::class.java)

        val inflater = this@WorkActivity.layoutInflater
        alertView = inflater.inflate(R.layout.alert_layout2, null)

        adapter.setClickListener(object :ListAdapter.clickedListener{
            override fun modifyItemData(id:Int, item_name: String, sort_id: Int, sort_name: String, price: Int, stock: Int, pic: String ) {

                val item_name2 = alertView.edItem_name2
                val sort_id2 = alertView.edSort_id2
                val price2 = alertView.edPrice2
                val stock2 = alertView.edStock2
                val pic2 = alertView.edPic2
                val btnSubmitEdit = alertView.textAdd2
                val btnCancelEdit = alertView.textCancel2

                val dialog = AlertDialog.Builder(this@WorkActivity)
                    .setView(alertView)
                    .create()

                item_name2.setText(item_name)
                sort_id2.setText(sort_id.toString())
                price2.setText(price.toString())
                stock2.setText(stock.toString())
                pic2.setText(pic)

                btnSubmitEdit.setOnClickListener {
                    btnCancelEdit.isEnabled = false
                    btnSubmitEdit.isEnabled = false

                    apiInterface.modify(
                        id.toString(),
                        token,
                        item_name2.text.toString(),
                        sort_id2.text.toString().toInt(),
                        price2.text.toString().toInt(),
                        stock2.text.toString().toInt(),
                        pic2.text.toString()
                    ).enqueue(object : Callback<ModifyData>{
                        override fun onFailure(call: Call<ModifyData>, t: Throwable) {
                            btnCancelEdit.isEnabled = true
                            btnSubmitEdit.isEnabled = true
                        }

                        override fun onResponse(call: Call<ModifyData>, response: Response<ModifyData>) {
                            if (response.isSuccessful){
                                val data = response.body()
                                Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
                                fetchAlbum()
                                dialog.dismiss()
                            }
                        }
                    })
                }
                btnCancelEdit.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()

            }

            override fun removeItemData(id:Int) {
                apiInterface
                    .delete(id.toString(), token)
                    .enqueue(object : Callback<DeleteData>{
                    override fun onFailure(call: Call<DeleteData>, t: Throwable) {
                        Toast.makeText(this@WorkActivity,"Unable to remove item $id",Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<DeleteData>, response: Response<DeleteData>) {
                        if (response.isSuccessful){
                            val data = response.body()
                            Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
                            fetchAlbum()
                        }
                    }
                })
            }
        })

        btnAdd.setOnClickListener {
            pickImageFromGallery()
        }

        fetchAlbum()

    }

    private fun fetchAlbum() {
        val call = apiInterface.getList()

        call.enqueue(object : Callback<ListData> {
            override fun onFailure(call: Call<ListData>, t: Throwable) {
                Toast.makeText(this@WorkActivity,"Unable to fetch album data from api",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
                if (response.code() == 200) {
                    val data = response.body()
                    list.clear()
                    for (i in 0 until data!!.items.size) {
                        list.add(
                            i,
                            CellItem(
                                data.items[i].id,
                                data.items[i].sort_id,
                                array[data.items[i].sort_id - 1],
                                data.items[i].item_name,
                                data.items[i].price,
                                data.items[i].stock ?: 0,
                                data.items[i].pic ?: ""
                            )
                        )
                    }
                    adapter.update(list)
                }
            }
        })
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
//        val intent = Intent() //呼叫照相機
//        intent.action = "android.media.action.STILL_IMAGE_CAMERA"
//        startActivity(intent)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data!= null){

            val inflater = this@WorkActivity.layoutInflater
            val view = inflater.inflate(R.layout.alert_layout, null)
            val imgUpload = view.findViewById<ImageView>(R.id.imageUpload)
            val item_name = view.findViewById<EditText>(R.id.edItem_name)
            val sort_id = view.findViewById<EditText>(R.id.edSort_id)
            val stock = view.findViewById<EditText>(R.id.edStock)
            val price = view.findViewById<EditText>(R.id.edPrice)
            val btnSubmitUpload = view.findViewById<TextView>(R.id.textAdd)
            val btnCancelUpload = view.findViewById<TextView>(R.id.textCancel)
            val photoUri = data.data!!

            btnSubmitUpload.isEnabled = false

            Glide.with(this)
                .asBitmap()
                .load(photoUri)
                .downsample(DownsampleStrategy.DEFAULT)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean ): Boolean {

                        val stream = ByteArrayOutputStream()
                        resource?.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                        val byteArray = stream.toByteArray()

                        resource?.recycle()
                        runOnUiThread{
                            bitmap = byteArray
                            btnSubmitUpload.isEnabled = true
                        }
                        return true
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        Toast.makeText(this@WorkActivity,"Unable to load image",Toast.LENGTH_SHORT).show()

                        return true
                    }

                })
                .submit()


            Glide.with(view)
                .load(photoUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(RoundedCorners(10))
                .into(imgUpload)

            val dialog = AlertDialog.Builder(this@WorkActivity)
                .setView(view)
                .create()

            btnSubmitUpload.setOnClickListener {
                if (item_name.text.isNullOrEmpty() or sort_id.text.isNullOrEmpty() or stock.text.isNullOrEmpty() or price.text.isNullOrEmpty()){
                    Toast.makeText(this@WorkActivity, "你又漏填了什麼...", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), bitmap!!)
                val body: MultipartBody.Part = MultipartBody.Part.createFormData("pic", "sample.png", requestFile)

                val additem_name: MultipartBody.Part = MultipartBody.Part.createFormData("item_name", item_name.text.toString())
                val addsort_id: MultipartBody.Part = MultipartBody.Part.createFormData("sort_id", sort_id.text.toString())
                val addprice: MultipartBody.Part = MultipartBody.Part.createFormData("price", price.text.toString())
                val addstock: MultipartBody.Part = MultipartBody.Part.createFormData("stock", stock.text.toString())

                apiInterface
                    .add(additem_name, addsort_id, addprice, addstock, body, token)
                    .enqueue(object :retrofit2.Callback<AddData>{
                    override fun onFailure(call: Call<AddData>, t: Throwable) {
                        Log.e("api", t.message)
                    }
                    override fun onResponse(call: Call<AddData>, response: Response<AddData>) {
                        Log.i("addapi", "succed ${response.code()}")
                        if (response.isSuccessful){
                            val data = response.body()
                            Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
                            fetchAlbum()

                            dialog.dismiss()
                        }
                    }
                })

            }
            btnCancelUpload.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }
    }
}
