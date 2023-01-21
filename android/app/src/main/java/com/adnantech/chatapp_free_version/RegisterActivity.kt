package com.adnantech.chatapp_free_version

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.adnantech.chatapp_free_version.models.GeneralResponse
import com.adnantech.chatapp_free_version.utils.MySharedPreference
import com.adnantech.chatapp_free_version.utils.Utility
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.Charset


class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var layoutLogin: RelativeLayout
    private lateinit var generalResponse: GeneralResponse
    private lateinit var mySharedPreference: MySharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_register)

        mySharedPreference = MySharedPreference()

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        layoutLogin = findViewById(R.id.layoutLogin)
        layoutLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRegister.setOnClickListener {
            btnRegister.isEnabled = false

            val queue = Volley.newRequestQueue(this)
            val url = mySharedPreference.getAPIURL(this) + "/register"

            val requestBody =
                "name=" + etName.text + "&phone=" + URLEncoder.encode(
                    etPhone.text.toString(),
                    "UTF-8"
                ) + "&password=" + etPassword.text
            val stringReq: StringRequest =
                object : StringRequest(Method.POST, url,
                    Response.Listener { response ->
                        btnRegister.isEnabled = true

                        generalResponse = Gson().fromJson(response, GeneralResponse::class.java)
                        if (generalResponse.status == "success") {
                            Utility.showAlert(
                                this,
                                "Registered",
                                generalResponse.message
                            )
                        } else {
                            Utility.showAlert(this, "Error", generalResponse.message)
                        }
                    },
                    Response.ErrorListener { error ->
                        Log.i("myLog", "error = " + error)
                    }
                ) {
                    override fun getBody(): ByteArray {
                        return requestBody.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
        }
    }
}
