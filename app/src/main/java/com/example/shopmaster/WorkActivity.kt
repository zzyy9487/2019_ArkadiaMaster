package com.example.shopmaster

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class WorkActivity : AppCompatActivity() {

    lateinit var adapter:ListAdapter
    lateinit var shared :SharedPreferences
    lateinit var addBody:AddBody
    lateinit var uploadBody:UploadBody
    var bitmap:ByteArray? = null
    lateinit var photoUri : Uri
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
        val apiInterface = retrofit.create(APIInterface::class.java)
        val call = apiInterface.getList()

        call.enqueue(object :retrofit2.Callback<ListData>{
            override fun onFailure(call: Call<ListData>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
                if (response.code() == 200){
                    val data = response.body()
                    list.clear()
                    for (i in 0 until data!!.items.size){
                    list.add(i, CellItem(data.items[i].id, data.items[i].sort_id, array[data.items[i].sort_id-1], data.items[i].item_name, data.items[i].price, data.items[i].stock?:0, data.items[i].pic?:""))
                    }
                    adapter.update(list)
                }
            }
        })

        adapter.setclickedListener(object :ListAdapter.clickedListener{
            override fun modifyItemData(id:Int, item_name: String, sort_id: Int, sort_name: String, price: Int, stock: Int, pic: String ) {
                val inflater = this@WorkActivity.layoutInflater
                val view = inflater.inflate(R.layout.alert_layout2, null)
                val builder = AlertDialog.Builder(this@WorkActivity)
                    .setView(view)
                    .show()
                val item_name2 = view.findViewById<EditText>(R.id.edItem_name2)
                val sort_id2 = view.findViewById<EditText>(R.id.edSort_id2)
                val price2 = view.findViewById<EditText>(R.id.edPrice2)
                val stock2 = view.findViewById<EditText>(R.id.edStock2)
                val pic2 = view.findViewById<EditText>(R.id.edPic2)
                val add2 = view.findViewById<TextView>(R.id.textAdd2)
                val cancel2 = view.findViewById<TextView>(R.id.textCancel2)

                item_name2.setText(item_name)
                sort_id2.setText(sort_id.toString())
                price2.setText(price.toString())
                stock2.setText(stock.toString())
                pic2.setText(pic)

                add2.setOnClickListener {
                    val retrofit6 = Retrofit.Builder()
                        .baseUrl("http://35.234.60.173")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val apiInterface6 = retrofit6.create(APIInterface::class.java)
                    val call6 = apiInterface6.modify(id.toString(), token, item_name2.text.toString()?:item_name, sort_id2.text.toString().toInt()?:sort_id, price2.text.toString().toInt()?:price, stock2.text.toString().toInt()?:stock, pic2.text.toString()?:pic)

                    call6.enqueue(object :retrofit2.Callback<ModifyData>{
                        override fun onFailure(call: Call<ModifyData>, t: Throwable) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onResponse(call: Call<ModifyData>, response: Response<ModifyData>) {
                            if (response.code() == 200){
                                val data = response.body()
                                Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
                                val retrofit7 = Retrofit.Builder()
                                    .baseUrl("http://35.234.60.173")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                                val apiInterface7 = retrofit7.create(APIInterface::class.java)
                                val call7 = apiInterface7.getList()

                                call7.enqueue(object :retrofit2.Callback<ListData>{
                                    override fun onFailure(call: Call<ListData>, t: Throwable) {
                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    }
                                    override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
                                        if (response.code() == 200){
                                            val data = response.body()
                                            list.clear()
                                            for (i in 0 until data!!.items.size){
                                                list.add(i, CellItem(data.items[i].id, data.items[i].sort_id, array[data.items[i].sort_id-1], data.items[i].item_name, data.items[i].price, data.items[i].stock?:0, data.items[i].pic?:""))
                                            }
                                            adapter.update(list)
                                        }
                                    }
                                })
                                builder.dismiss()

                            }
                        }
                    })
                }

                cancel2.setOnClickListener {
                    builder.dismiss()
                }
            }

            override fun removeItemData(id:Int) {
                val retrofit2 = Retrofit.Builder()
                    .baseUrl("http://35.234.60.173")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface2 = retrofit2.create(APIInterface::class.java)
                val call2 = apiInterface2.delete(id.toString(), token)
                call2.enqueue(object :retrofit2.Callback<DeleteData>{
                    override fun onFailure(call: Call<DeleteData>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call<DeleteData>, response: Response<DeleteData>) {

                        if (response.isSuccessful){
                            val data = response.body()
                            Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
                            val retrofit3 = Retrofit.Builder()
                                .baseUrl("http://35.234.60.173")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                            val apiInterface3 = retrofit3.create(APIInterface::class.java)
                            val call3 = apiInterface3.getList()

                            call3.enqueue(object :retrofit2.Callback<ListData>{
                                override fun onFailure(call: Call<ListData>, t: Throwable) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }
                                override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
                                    if (response.code() == 200){
                                        val data = response.body()
                                        list.clear()
                                        for (i in 0 until data!!.items.size){
                                            list.add(i, CellItem(data.items[i].id, data.items[i].sort_id, array[data.items[i].sort_id-1], data.items[i].item_name, data.items[i].price, data.items[i].stock?:0, data.items[i].pic?:""))
                                        }
                                        adapter.update(list)
                                    }
                                }
                            })
                        }
                    }
                })
            }
        })

        btnAdd.setOnClickListener {
            pickImageFromGallery()

        }


//        btnAdd.setOnClickListener {
//            val inflater = this@WorkActivity.layoutInflater
//            val view = inflater.inflate(R.layout.alert_layout, null)
//            val builder = AlertDialog.Builder(this@WorkActivity)
//                .setView(view)
//                .show()
//            val imgUpload = view.findViewById<ImageView>(R.id.imageUpload)
//            val item_name = view.findViewById<EditText>(R.id.edItem_name)
//            val sort_id = view.findViewById<EditText>(R.id.edSort_id)
//            val stock = view.findViewById<EditText>(R.id.edStock)
//            val price = view.findViewById<EditText>(R.id.edPrice)
//            val add = view.findViewById<TextView>(R.id.textAdd)
//            val cancel = view.findViewById<TextView>(R.id.textCancel)
//
//            imgUpload.setOnClickListener {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
//                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//                        requestPermissions(permissions, PERMISSION_CODE)
//                    }
//                    else{
//                        pickImageFromGallery()
//
//                        Glide.with(view).load(photosourceUri)
//                            .transform(RoundedCorners(10))
//                            .into(imgUpload)
//                    }
//                }
//                else{
//                    pickImageFromGallery()
//                    Glide.with(view).load(photosourceUri)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .transform(RoundedCorners(10))
//                        .into(imgUpload)
//                }
//
//
//
//
//            }
//
//
//
//            add.setOnClickListener {
//                if (item_name.text.isNullOrEmpty() or sort_id.text.isNullOrEmpty() or stock.text.isNullOrEmpty() or price.text.isNullOrEmpty()){
//                    Toast.makeText(this@WorkActivity, "你又漏填了什麼...", Toast.LENGTH_LONG).show()
//                }
//                else{
//                    addBody = AddBody(item_name.text.toString(), sort_id.text.toString().toInt(), price.text.toString().toInt(), stock.text.toString().toInt())
//                    val retrofit4 = Retrofit.Builder()
//                        .baseUrl("http://35.234.60.173")
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build()
//                    val apiInterface4 = retrofit4.create(APIInterface::class.java)
//                    val call4 = apiInterface4.add(addBody, token)
//                    call4.enqueue(object :retrofit2.Callback<AddData>{
//                        override fun onFailure(call: Call<AddData>, t: Throwable) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                        }
//
//                        override fun onResponse(call: Call<AddData>, response: Response<AddData>) {
//                            if (response.code() == 201){
//                                val data = response.body()
//                                Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
//
//                                uploadBody = UploadBody(data.data.id, )
//                                val retrofit99 = Retrofit.Builder()
//                                    .baseUrl("http://35.234.60.173")
//                                    .addConverterFactory(GsonConverterFactory.create())
//                                    .build()
//                                val apiInterface99 = retrofit99.create(APIInterface::class.java)
//                                val call99 = apiInterface99.upload(token, uploadBody)
//
//                                call99.enqueue(object :retrofit2.Callback<UploadData>{
//                                    override fun onFailure(call: Call<UploadData>, t: Throwable) {
//                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                                    }
//
//                                    override fun onResponse(call: Call<UploadData>, response: Response<UploadData>) {
//                                        if (response.isSuccessful){
//                                            Toast.makeText(this@WorkActivity, "!!!", Toast.LENGTH_LONG).show()
//                                        }
//                                    }
//
//                                })
//
//                                val retrofit5 = Retrofit.Builder()
//                                    .baseUrl("http://35.234.60.173")
//                                    .addConverterFactory(GsonConverterFactory.create())
//                                    .build()
//                                val apiInterface5 = retrofit5.create(APIInterface::class.java)
//                                val call5 = apiInterface5.getList()
//
//                                call5.enqueue(object :retrofit2.Callback<ListData>{
//                                    override fun onFailure(call: Call<ListData>, t: Throwable) {
//                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                                    }
//                                    override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
//                                        if (response.code() == 200){
//                                            val data = response.body()
//                                            list.clear()
//                                            for (i in 0 until data!!.items.size){
//                                                list.add(i, CellItem(data.items[i].id, data.items[i].sort_id, array[data.items[i].sort_id-1], data.items[i].item_name, data.items[i].price, data.items[i].stock?:0, data.items[i].pic?:""))
//                                            }
//                                            adapter.update(list)
//                                        }
//                                    }
//                                })
//
//                            }
//                        }
//                    })
//                    builder.dismiss()
//                }
//
//            }
//
//            cancel.setOnClickListener {
//                builder.dismiss()
//            }
//
//
//        }
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
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            if (data != null) {
                photoUri = data.data!!
                Glide.with(this)
                    .asBitmap()
                    .load(photoUri)
                    .downsample(DownsampleStrategy.DEFAULT)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean ): Boolean {

                            val stream = ByteArrayOutputStream()
                            resource?.compress(Bitmap.CompressFormat.JPEG, 60, stream)

                            val byteArray = stream.toByteArray()
                            println("**************** ${byteArray.size}")

                            resource?.recycle()
                            runOnUiThread(){
                                bitmap = byteArray
                            }
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            return true
                        }

                    }).submit()
                val inflater = this@WorkActivity.layoutInflater
                val view = inflater.inflate(R.layout.alert_layout, null)
                val imgUpload = view.findViewById<ImageView>(R.id.imageUpload)
                val item_name = view.findViewById<EditText>(R.id.edItem_name)
                val sort_id = view.findViewById<EditText>(R.id.edSort_id)
                val stock = view.findViewById<EditText>(R.id.edStock)
                val price = view.findViewById<EditText>(R.id.edPrice)
                val add = view.findViewById<TextView>(R.id.textAdd)
                val cancel = view.findViewById<TextView>(R.id.textCancel)
                Glide.with(view).load(photoUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(RoundedCorners(10))
                    .into(imgUpload)
                val builder = AlertDialog.Builder(this@WorkActivity)
                    .setView(view)
                    .show()
                add.setOnClickListener {
                    if (item_name.text.isNullOrEmpty() or sort_id.text.isNullOrEmpty() or stock.text.isNullOrEmpty() or price.text.isNullOrEmpty()){
                        Toast.makeText(this@WorkActivity, "你又漏填了什麼...", Toast.LENGTH_LONG).show()
                    }
                    else{
//                        var result: String? = null
//                        var cursor = this@WorkActivity.contentResolver.query(photoUri, null, null, null, null)
//                        if (cursor != null) {
//                           if( cursor.moveToFirst()){
//                                var idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
//                                result = cursor.getString(idx)
//                                cursor.close()
//                           }
//                        }
//                        var file = File(result)
                        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), bitmap!!)
                        val body: MultipartBody.Part = MultipartBody.Part.createFormData("pic", "sample.png", requestFile)
                        val retrofit4 = Retrofit.Builder()
                            .baseUrl("http://35.234.60.173")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                        val apiInterface4 = retrofit4.create(APIInterface::class.java)
                        val additem_name: MultipartBody.Part = MultipartBody.Part.createFormData("item_name", item_name.text.toString())
                        val addsort_id: MultipartBody.Part = MultipartBody.Part.createFormData("sort_id", sort_id.text.toString())
                        val addprice: MultipartBody.Part = MultipartBody.Part.createFormData("price", price.text.toString())
                        val addstock: MultipartBody.Part = MultipartBody.Part.createFormData("stock", stock.text.toString())
                        val call4 = apiInterface4.add(additem_name, addsort_id, addprice, addstock, body, token)
                        call4.enqueue(object :retrofit2.Callback<AddData>{
                            override fun onFailure(call: Call<AddData>, t: Throwable) {
                                Log.e("api", t.message)
                            }
                            override fun onResponse(call: Call<AddData>, response: Response<AddData>) {
                                Log.i("addapi", "succed ${response.code()}")
                                if (response.isSuccessful){
                                    val data = response.body()
                                    Toast.makeText(this@WorkActivity, data!!.msg, Toast.LENGTH_LONG).show()
                                    // 檔案新增成功後,接續上傳圖片
//                                if (bitmap != null) {
//                                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), bitmap!!)
//                                    val body: MultipartBody.Part = MultipartBody.Part.createFormData("pic", "sample.png", requestFile)
//                                    uploadBody = UploadBody(data.data.id, body)
//                                    val retrofit99 = Retrofit.Builder()
//                                        .baseUrl("http://35.234.60.173")
//                                        .addConverterFactory(GsonConverterFactory.create())
//                                        .build()
//                                    val apiInterface99 = retrofit99.create(APIInterface::class.java)
//                                    val call99 = apiInterface99.upload(token, uploadBody)
//
//                                    call99.enqueue(object : retrofit2.Callback<UploadData> {
//                                        override fun onFailure(call: Call<UploadData>, t: Throwable) {
//                                            Toast.makeText(this@WorkActivity, t.toString(), Toast.LENGTH_LONG).show()
//                                        }
//
//                                        override fun onResponse(call: Call<UploadData>, response: Response<UploadData>) {
//                                            if (response.isSuccessful) {
//                                                Toast.makeText(this@WorkActivity, "!!!", Toast.LENGTH_LONG).show()
//                                            }
//                                        }
//
//
//                                    })
//                                }
                                    val retrofit5 = Retrofit.Builder()
                                        .baseUrl("http://35.234.60.173")
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()
                                    val apiInterface5 = retrofit5.create(APIInterface::class.java)
                                    val call5 = apiInterface5.getList()

                                    call5.enqueue(object :retrofit2.Callback<ListData>{
                                        override fun onFailure(call: Call<ListData>, t: Throwable) {
                                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                        }
                                        override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
                                            if (response.code() == 200){
                                                val data = response.body()
                                                list.clear()
                                                for (i in 0 until data!!.items.size){
                                                    list.add(i, CellItem(data.items[i].id, data.items[i].sort_id, array[data.items[i].sort_id-1], data.items[i].item_name, data.items[i].price, data.items[i].stock?:0, data.items[i].pic?:""))
                                                }
                                                adapter.update(list)
                                            }
                                        }
                                    })
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


        }
    }





}
