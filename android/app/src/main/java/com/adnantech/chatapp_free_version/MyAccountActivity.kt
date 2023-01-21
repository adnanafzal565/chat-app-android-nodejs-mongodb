package com.adnantech.chatapp_free_version

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.adnantech.chatapp.utils.FetchImageFromInternet
import com.adnantech.chatapp_free_version.models.GeneralResponse
import com.adnantech.chatapp_free_version.models.GetUserResponse
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.utils.MySharedPreference
import com.adnantech.chatapp_free_version.utils.Utility
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class MyAccountActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var image: ImageView
    private lateinit var btnUpdate: Button

    var base64: String = ""
    private lateinit var selectedAttachment: Uri
    var attachmentName: String = ""
    var extension: String = ""
    lateinit var mySharedPreference: MySharedPreference
    lateinit var user: User
    var isFirstTime: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        title = "My profile"
        mySharedPreference = MySharedPreference()
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        image = findViewById(R.id.image)

        isFirstTime = mySharedPreference.isFirstTime(this)
        mySharedPreference.setIsFirstTime(this)

        val layoutImage: RelativeLayout = findViewById(R.id.layoutImage)
        layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 565)
        }

        btnUpdate = findViewById(R.id.btnUpdate)
        btnUpdate.setOnClickListener {
            btnUpdate.isEnabled = false

            val queue = Volley.newRequestQueue(this)
            val url = mySharedPreference.getAPIURL(this) + "/updateProfile"

            val requestBody =
                "name=" + etName.text + "&base64=" + base64 + "&attachmentName=" + attachmentName + "&extension=" + extension
            val stringReq: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->

                        btnUpdate.isEnabled = true

                        val responseObj: GeneralResponse =
                            Gson().fromJson(response, GeneralResponse::class.java)

                        if (responseObj.status == "success") {
                            if (isFirstTime) {
                                val dialog = Dialog(this)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setCancelable(false)
                                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                dialog.setContentView(R.layout.dialog_account_setup)

                                val image: ImageView = dialog.findViewById(R.id.image)
                                if (::selectedAttachment.isInitialized) {
                                    image.setImageURI(selectedAttachment)
                                } else if (user.image.isNotEmpty()) {
                                    FetchImageFromInternet(image).execute(user.image)
                                }
                                dialog.show()

                                Timer().schedule(3000) {
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            } else {
                                Utility.showAlert(
                                    this,
                                    "My profile",
                                    responseObj.message
                                )
                            }
                        } else {
                            Utility.showAlert(
                                this,
                                "Error",
                                responseObj.message
                            )
                        }

                    },
                    Response.ErrorListener { error ->
                        Log.i("mylog", "error = " + error)
                    }
                ) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer " +
                            mySharedPreference.getAccessToken(
                                applicationContext
                            )
                        return headers
                    }

                    override fun getBody(): ByteArray {
                        return requestBody.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
        }

        val queue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/getUser"

        val stringReq: StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->

                    val responseObj: GetUserResponse =
                        Gson().fromJson(response, GetUserResponse::class.java)
                    if (responseObj.status == "success") {
                        user = responseObj.user
                        etName.setText(user.name)
                        etPhone.setText(user.phone)

                        if (user.image.isEmpty()) {
                            image.setImageResource(R.drawable.default_profile)
                        } else {
                            FetchImageFromInternet(image).execute(user.image)
                        }
                    } else {
                        Utility.showAlert(
                            applicationContext,
                            "Error",
                            responseObj.message
                        )
                    }

                },
                Response.ErrorListener { error ->
                    Log.i("mylog", "error = " + error)
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer " +
                        mySharedPreference.getAccessToken(
                            applicationContext
                        )
                    return headers
                }
            }
        queue.add(stringReq)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 565) {
            val uri: Uri? = data?.data

            if (uri != null) {
                selectedAttachment = uri
                image.setImageURI(selectedAttachment)
            }

            base64 = ""
            try {
                val bytes = uri?.let { contentResolver?.openInputStream(it)?.readBytes() }
                base64 = Base64.encodeToString(bytes, Base64.URL_SAFE)
            } catch (e1: IOException) {
                e1.printStackTrace()
            }

            attachmentName =
                uri?.let { Utility.getFileName(it, contentResolver) }
                    .toString()

            extension = uri?.let {
                Utility.getMimeType(
                    context = applicationContext,
                    it
                )
            }.toString()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent: Intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
        return true
    }
}
