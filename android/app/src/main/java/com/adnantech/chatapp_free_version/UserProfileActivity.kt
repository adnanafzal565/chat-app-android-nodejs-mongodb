package com.adnantech.chatapp_free_version

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.adnantech.chatapp.utils.FetchImageFromInternet
import com.adnantech.chatapp_free_version.models.FetchUserResponse
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.utils.LoadingDialog
import com.adnantech.chatapp_free_version.utils.MySharedPreference
import com.adnantech.chatapp_free_version.utils.Utility
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap


class UserProfileActivity : AppCompatActivity() {

    lateinit var image: ImageView
    lateinit var name: TextView
    lateinit var phone: TextView
    lateinit var joiningDate: TextView

    lateinit var loadingDialog: LoadingDialog
    var _id: String = ""
    lateinit var mySharedPreference: MySharedPreference
    lateinit var user: User
    lateinit var me: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        title = ""
        mySharedPreference = MySharedPreference()

        image = findViewById(R.id.image)
        name = findViewById(R.id.name)
        phone = findViewById(R.id.phone)
        joiningDate = findViewById(R.id.joiningDate)

        loadingDialog = LoadingDialog(this)

        if (intent.hasExtra("_id")) {
            _id = intent.getStringExtra("_id").toString()
            loadingDialog.show()

            val queue: RequestQueue = Volley.newRequestQueue(this)
            val url = mySharedPreference.getAPIURL(this) + "/fetchUser"
            val requestBody: String = "_id=$_id"

            val request: StringRequest = @RequiresApi(Build.VERSION_CODES.N)
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    val fetchUserResponse: FetchUserResponse =
                        Gson().fromJson(response, FetchUserResponse::class.java)
                    if (fetchUserResponse.status == "success") {
                        user = fetchUserResponse.user
                        me = fetchUserResponse.me

                        name.text = user.name
                        phone.text = user.phone

                        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH)
                        cal.timeInMillis = user.createdAt
                        val date: String = DateFormat.format("MMMM dd, yyyy", cal).toString()
                        joiningDate.text = date

                        if (user.image.isEmpty()) {
                            image.setImageResource(R.drawable.default_profile)
                        } else {
                            FetchImageFromInternet(image).execute(user.image)
                        }
                    } else {
                        Utility.showAlert(this, "Error", fetchUserResponse.message)
                    }

                    loadingDialog.dismiss()
                },

                Response.ErrorListener { error ->
                    Log.i("myLog", "error => " + error)
                    loadingDialog.dismiss()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                    return headers
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
            queue.add(request)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent: Intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("_id", _id)
        startActivity(intent)
        finish()
        return true
    }
}
