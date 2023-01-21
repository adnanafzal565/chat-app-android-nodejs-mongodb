package com.adnantech.chatapp_free_version

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.adnantech.chatapp.utils.FetchImageFromInternet
import com.adnantech.chatapp_free_version.adapters.ChatAdapter
import com.adnantech.chatapp_free_version.models.FetchMessagesResponse
import com.adnantech.chatapp_free_version.models.Message
import com.adnantech.chatapp_free_version.models.SendMessageResponse
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.utils.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.socket.client.IO
import io.socket.client.Socket
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class ChatActivity : AppCompatActivity() {

    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter

    private var _id: String = ""
    private var name: String = ""
    private var phone: String = ""

    private val mySharedPreference: MySharedPreference = MySharedPreference()
    private lateinit var socket: Socket
    private lateinit var user: User
    private lateinit var receiver: User
    lateinit var image: ImageView

    lateinit var titleTv: TextView
    lateinit var loadingDialog: LoadingDialog
    var page: Int = 1
    var isLoaded: Boolean = false

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("InflateParams", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.hide()
        loadingDialog = LoadingDialog(this)

        titleTv = findViewById(R.id.title)
        image = findViewById(R.id.image)

        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnSend = findViewById(R.id.btnSend)
        btnSend.tag = R.drawable.ic_mic

        etMessage = findViewById(R.id.etMessage)
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun afterTextChanged(p0: Editable?) {
                if (etMessage.text.isEmpty()) {
                    btnSend.setImageResource(R.drawable.ic_mic)
                    btnSend.tag = R.drawable.ic_mic
                } else {
                    btnSend.setImageResource(R.drawable.ic_send)
                    btnSend.tag = R.drawable.ic_send
                }
            }
        })

        recyclerView = findViewById(R.id.recyclerView)
        // this creates a vertical layout Manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        adapter = ChatAdapter(this, ArrayList())

        // Setting the Adapter with the recyclerview
        recyclerView.adapter = adapter

        val pullToRefresh: SwipeRefreshLayout = findViewById(R.id.pullToRefresh)
        pullToRefresh.setOnRefreshListener {
            page++
            getData()
            pullToRefresh.isRefreshing = false
        }

        if (intent.hasExtra("_id")) {
            _id = intent.getStringExtra("_id").toString()
            name = intent.getStringExtra("name").toString()
            phone = intent.getStringExtra("phone").toString()
            phone = URLEncoder.encode(phone, "UTF-8")

            getData()

            val userInfo: RelativeLayout = findViewById(R.id.userInfo)
            userInfo.setOnClickListener {
                val intent: Intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra("_id", _id)
                startActivity(intent)
                finish()
            }

            btnSend.setOnClickListener {
                saveMessage()
            }
        }

        try {
            socket = IO.socket(mySharedPreference.getAPIURL(this))
            socket.connect()

            socket.on("messageRead") { data ->

                if (data.isNotEmpty()) {
                    runOnUiThread {
                        val message: Message =
                            Gson().fromJson(data[0].toString(), Message::class.java)
                        if (::adapter.isInitialized && ::user.isInitialized && ::recyclerView.isInitialized) {
                            val messages: ArrayList<Message> = adapter.getMessages()
                            for (msg in messages) {
                                if (msg._id == message._id) {
                                    msg.isRead = true
                                }
                            }
                            adapter.setMessages(messages, user)
                        }
                    }
                }
            }

            socket.on("newMessage") { data ->
                if (data.isNotEmpty()) {
                    runOnUiThread {
                        val message: Message =
                            Gson().fromJson(data[0].toString(), Message::class.java)
                        if (::adapter.isInitialized && ::user.isInitialized && ::recyclerView.isInitialized) {
                            adapter.appendMessage(message, user)
                            recyclerView.scrollToPosition(adapter.getMessages().size - 1)

                            val messageObject: Message = Message()
                            messageObject._id = message._id
                            messageObject.message = Utility.decryptMessage(message)
                            messageObject.sender = message.sender
                            messageObject.receiver = message.receiver
                            messageObject.attachment = message.attachment
                            messageObject.attachmentName = message.attachmentName
                            messageObject.createdAt = message.createdAt

                            socket.emit("messageRead", Gson().toJson(message))
                        }
                    }
                }
            }
        } catch (e: URISyntaxException) {
//            Log.i("mylog", e.message.toString())
        }
    }

    private fun getData() {
        loadingDialog.show()

        val request: RequestQueue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/chats/fetch"
        val requestBody: String = "userId=" + _id + "&page=" + page

        val queue: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                val fetchMessagesResponse: FetchMessagesResponse =
                    Gson().fromJson(response, FetchMessagesResponse::class.java)
                if (fetchMessagesResponse.status == "success") {
                    user = fetchMessagesResponse.user
                    receiver = fetchMessagesResponse.receiver

                    if (!isLoaded) {
                        adapter.setReceiver(receiver)
                        titleTv.text = receiver.name

                        socket.emit("connected", user._id)
                        socket.emit("allMessagesRead", receiver._id, user._id)

                        if (receiver.image.isEmpty()) {
                            image.setImageResource(R.drawable.default_profile)
                        } else {
                            FetchImageFromInternet(image).execute(receiver.image)
                        }

                        socket.on("allMessagesRead-" + receiver._id) { data ->

                            if (data.isNotEmpty()) {
                                runOnUiThread {
                                    if (::adapter.isInitialized && ::user.isInitialized && ::recyclerView.isInitialized) {

                                        val messages: ArrayList<Message> = adapter.getMessages()
                                        for (msg in messages) {
                                            msg.isRead = true
                                        }
                                        adapter.setMessages(messages, user)
                                    }
                                }
                            }
                        }
                    }

                    if (isLoaded) {
                        for (message in fetchMessagesResponse.messages.reversed()) {
                            adapter.prependMessage(message)
                        }
                    } else {
                        adapter.setMessages(fetchMessagesResponse.messages, user)
                        recyclerView.scrollToPosition(fetchMessagesResponse.messages.size - 1)
                    }
                    isLoaded = true
                } else {
                    Utility.showAlert(this, "Error", fetchMessagesResponse.message)
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
        request.add(queue)
    }

    private fun saveMessage() {
        btnSend.isEnabled = false

        val request: RequestQueue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/chats/send"
        val requestBody: String =
            "message=" + etMessage.text + "&userId=" + _id

        val queue: StringRequest = @RequiresApi(Build.VERSION_CODES.P)
        object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                val sendMessageResponse: SendMessageResponse =
                    Gson().fromJson(response, SendMessageResponse::class.java)

                btnSend.isEnabled = true

                if (sendMessageResponse.status == "success") {
                    socket.emit("newMessage", Gson().toJson(sendMessageResponse.messageObj))

                    adapter.appendMessage(sendMessageResponse.messageObj, sendMessageResponse.user)
                    recyclerView.scrollToPosition(adapter.getMessages().size - 1)

                    val messageObject: Message = Message()
                    messageObject._id = sendMessageResponse.messageObj._id
                    messageObject.message = etMessage.text.toString()
                    messageObject.sender = sendMessageResponse.messageObj.sender
                    messageObject.receiver = sendMessageResponse.messageObj.receiver
                    messageObject.attachment = sendMessageResponse.messageObj.attachment
                    messageObject.attachmentName = sendMessageResponse.messageObj.attachmentName
                    messageObject.createdAt = sendMessageResponse.messageObj.createdAt

                    etMessage.setText("")
                } else {
                    Utility.showAlert(this, "Error", sendMessageResponse.message)
                }
            },

            Response.ErrorListener { error ->
                Log.i("myLog", "error => " + error)
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
        request.add(queue)
    }

    override fun onSupportNavigateUp(): Boolean {
        socket.disconnect()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
        return true
    }
}
