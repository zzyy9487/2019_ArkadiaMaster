package com.example.shopmaster

import android.Manifest
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.shopmaster.add.AddData
import com.example.shopmaster.delete.DeleteData
import com.example.shopmaster.getList.CellItem
import com.example.shopmaster.getList.ListAdapter
import com.example.shopmaster.getList.ListData
import com.example.shopmaster.modify.ModifyData
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
import java.io.File
import java.lang.Exception

class WorkActivity : AppCompatActivity() {

    private lateinit var alertView: View
    private lateinit var apiInterface: APIInterface
    lateinit var adapter:ListAdapter
    lateinit var shared :SharedPreferences
    var bitmap:ByteArray? = null
    var token :String =""
    var saveUri : Uri? = null

    companion object {
        private val PHOTO_FROM_ALBUM = 0
        private val PHOTO_FROM_CAMERA = 1
        private val PERMISSION_CODE = 2
        private val PHOTO_FROM_GALLERY = 3
    }

     open inner class RefreshCallback<T>: Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            Toast.makeText(this@WorkActivity, "Unable to fetch data from API",Toast.LENGTH_SHORT).show()
            doAfterFailure()
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful){
                getRawList()
                doAfterSuccess(response)
            }
        }

        open fun doAfterSuccess(response: Response<T>) {

        }

        open fun doAfterFailure(){

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        shared = SharedPreferences(this)
        if (savedInstanceState != null) {
            saveUri = Uri.parse(savedInstanceState.getString("saveUri"))
        }
        permission()
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

                val modify_item_name = alertView.edItem_name2
                val modify_sort_id = alertView.edSort_id2
                val modify_price = alertView.edPrice2
                val modify_stock = alertView.edStock2
                val modify_pic = alertView.edPic2
                val modify_btnEdit = alertView.textAdd2
                val modify_btnCancel = alertView.textCancel2

                val dialog = AlertDialog.Builder(this@WorkActivity)
                    .setView(alertView)
                    .create()

                modify_item_name.setText(item_name)
                modify_sort_id.setText(sort_id.toString())
                modify_price.setText(price.toString())
                modify_stock.setText(stock.toString())
                modify_pic.setText(pic)

                modify_btnEdit.setOnClickListener {
                    modify_btnCancel.isEnabled = false
                    modify_btnEdit.isEnabled = false

                    apiInterface.modify(
                        id.toString(),
                        token,
                        modify_item_name.text.toString(),
                        modify_sort_id.text.toString().toInt(),
                        modify_price.text.toString().toInt(),
                        modify_stock.text.toString().toInt(),
                        modify_pic.text.toString()
                    ).enqueue(object : RefreshCallback<ModifyData>() {
                        override fun doAfterFailure() {
                            modify_btnCancel.isEnabled = true
                            modify_btnEdit.isEnabled = true
                        }

                        override fun doAfterSuccess(response: Response<ModifyData>) {
                            Toast.makeText(this@WorkActivity, response.body()!!.msg, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    })
                }
                modify_btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()

            }

            override fun removeItemData(id:Int) {
                apiInterface
                    .delete(id.toString(), token)
                    .enqueue(object :RefreshCallback<DeleteData>(){
                        override fun doAfterSuccess(response: Response<DeleteData>) {
                            Toast.makeText(this@WorkActivity, response.body()!!.msg, Toast.LENGTH_LONG).show()
                        }
                    })
            }
        })

        btnAddPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        btnAddCamera.setOnClickListener {
            getImageFromCamera()
        }

        getRawList()

        permission()
    }

    private fun getImageFromCamera() {
        val intent = Intent(ACTION_IMAGE_CAPTURE)
        val tmpFile = File(Environment.getExternalStorageDirectory().toString(), System.currentTimeMillis().toString() + ".jpg")
        val uriForCamera = FileProvider.getUriForFile(this, "com.example.shopmaster.fileprovider", tmpFile)
        saveUri = uriForCamera
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForCamera)
        startActivityForResult(intent, PHOTO_FROM_CAMERA)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (saveUri != null){
            val uriString = saveUri.toString()
            outState.putString("saveUri", uriString)
        }
    }

    private fun getRawList() {
        val call = apiInterface.getList()

        call.enqueue(object : Callback<ListData> {
            override fun onFailure(call: Call<ListData>, t: Throwable) {
                Toast.makeText(this@WorkActivity,"Unable to fetch album data from api",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ListData>, response: Response<ListData>) {
                if (response.code() == 200) {
                    val sortTypeMap = mapOf(1 to "糧食", 2 to "軍事", 3 to  "特殊", 4 to "隱藏組合")
                    val list = response.body()!!.items
                        .map {
                            CellItem(
                                it.id,
                                it.sort_id,
                                it.sort_id.let { sortTypeMap.getOrDefault(it,"Unknown") },
                                it.item_name,
                                it.price,
                                it.stock?:0,
                                it.pic?:""
                            )
                        }
                        .sortedBy { it.sort_id }
                    adapter.update(list)
                }
            }
        })
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            when (requestCode){
            PHOTO_FROM_CAMERA -> {
                when (resultCode){
                    Activity.RESULT_OK -> {

                        val inflater = this@WorkActivity.layoutInflater
                        val view = inflater.inflate(R.layout.alert_layout, null)
                        val imgUpload = view.findViewById<ImageView>(R.id.imageUpload)
                        val item_name = view.findViewById<EditText>(R.id.edItem_name)
                        val sort_id = view.findViewById<EditText>(R.id.edSort_id)
                        val stock = view.findViewById<EditText>(R.id.edStock)
                        val price = view.findViewById<EditText>(R.id.edPrice)
                        val btnSubmitUpload = view.findViewById<TextView>(R.id.textAdd)
                        val btnCancelUpload = view.findViewById<TextView>(R.id.textCancel)

                        btnSubmitUpload.isEnabled = false

                        Glide.with(this)
                            .asBitmap()
                            .load(saveUri)
                            .downsample(DownsampleStrategy.DEFAULT)
                            .listener(object : RequestListener<Bitmap> {
                                override fun onResourceReady(resource: Bitmap, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean ): Boolean {

                                    try {
                                        val stream = ByteArrayOutputStream()
                                        resource.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                                        stream.close()
                                        val byteArray = stream.toByteArray()
                                        runOnUiThread{
                                            bitmap = byteArray
                                            btnSubmitUpload.isEnabled = true
                                        }

                                    }catch (e:Exception){
                                        e.printStackTrace()
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
                            .load(saveUri)
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
                                .enqueue(object : RefreshCallback<AddData>() {
                                    override fun doAfterSuccess(response: Response<AddData>) {
                                        Toast.makeText(this@WorkActivity, response.body()!!.msg, Toast.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    }
                                })

                        }
                        btnCancelUpload.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()

                    }

                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResultFromCamera", resultCode.toString())
                    }
                }
            }

            PHOTO_FROM_ALBUM -> {
                when (resultCode){
                    Activity.RESULT_OK -> {
                        if (data==null)return
                        val inflater = this@WorkActivity.layoutInflater
                        val view = inflater.inflate(R.layout.alert_layout, null)
                        val imgUpload = view.findViewById<ImageView>(R.id.imageUpload)
                        val item_name = view.findViewById<EditText>(R.id.edItem_name)
                        val sort_id = view.findViewById<EditText>(R.id.edSort_id)
                        val stock = view.findViewById<EditText>(R.id.edStock)
                        val price = view.findViewById<EditText>(R.id.edPrice)
                        val btnSubmitUpload = view.findViewById<TextView>(R.id.textAdd)
                        val btnCancelUpload = view.findViewById<TextView>(R.id.textCancel)
                        val photoUri = data!!.data!!

                        btnSubmitUpload.isEnabled = false

                        Glide.with(this)
                            .asBitmap()
                            .load(photoUri)
                            .downsample(DownsampleStrategy.DEFAULT)
                            .listener(object : RequestListener<Bitmap> {
                                override fun onResourceReady(resource: Bitmap, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean ): Boolean {

                                    try {
                                        val stream = ByteArrayOutputStream()
                                        resource.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                                        stream.close()
                                        val byteArray = stream.toByteArray()
                                        runOnUiThread{
                                            bitmap = byteArray
                                            btnSubmitUpload.isEnabled = true
                                        }

                                    }catch (e:Exception){
                                        e.printStackTrace()
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
                                .enqueue(object : RefreshCallback<AddData>() {
                                    override fun doAfterSuccess(response: Response<AddData>) {
                                        Toast.makeText(this@WorkActivity, response.body()!!.msg, Toast.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    }
                                })

                        }
                        btnCancelUpload.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()


                    }

                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResultFromAlbum", resultCode.toString())
                    }
                }
            }

        }

    }

    fun permission() {
        val permissionList = arrayListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        var size = permissionList.size
        var i = 0
        while (i < size) {         //將三項存取權用迴圈一個一個判斷使用者是否同意存取
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permissionList[i]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.removeAt(i)
                i -= 1
                size -= 1
            }
            i += 1
        }
        val array = arrayOfNulls<String>(permissionList.size)
        if (permissionList.isNotEmpty()) ActivityCompat.requestPermissions(
            this,
            permissionList.toArray(array),
            0
        )
    }
}
