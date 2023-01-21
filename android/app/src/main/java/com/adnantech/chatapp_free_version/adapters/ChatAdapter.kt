package com.adnantech.chatapp_free_version.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.adnantech.chatapp_free_version.R
import com.adnantech.chatapp_free_version.models.Message
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.utils.Utility
import com.bumptech.glide.Glide
import java.io.IOException
import java.util.*


class ChatAdapter(
    var context: Context,
    private var messages: ArrayList<Message>
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private lateinit var user: User
    private lateinit var receiver: User

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_message, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Message = messages[position]
        val isMyMessage: Boolean = (item.sender._id == user._id)

        holder.message.isVisible = true
        holder.otherMessage.isVisible = true

        holder.message.text = item.message
        holder.otherMessage.text = item.message

        holder.time.text = DateUtils.getRelativeTimeSpanString(
            item.createdAt,
            Date().time,
            0L,
            DateUtils.FORMAT_ABBREV_ALL
        )
        holder.otherTime.text = DateUtils.getRelativeTimeSpanString(
            item.createdAt,
            Date().time,
            0L,
            DateUtils.FORMAT_ABBREV_ALL
        )

        if (isMyMessage) {
            holder.myLayout.isVisible = true
            holder.otherLayout.isVisible = false
        } else {
            holder.myLayout.isVisible = false
            holder.otherLayout.isVisible = true
        }
    }

    fun getMessages(): ArrayList<Message> {
        return this.messages
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(messages: ArrayList<Message>, user: User) {
        this.messages = messages
        this.user = user
        notifyDataSetChanged()
    }

    fun prependMessage(message: Message) {
        this.messages.add(0, message)
        notifyItemInserted(0)
    }

    fun setReceiver(receiver: User) {
        this.receiver = receiver
    }

    @SuppressLint("NotifyDataSetChanged")
    fun appendMessage(message: Message, user: User) {
        this.messages.add(message)
        this.user = user
        notifyItemInserted(this.messages.size)
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return messages.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val message: TextView = itemView.findViewById(R.id.message)
        val otherMessage: TextView = itemView.findViewById(R.id.otherMessage)
        val myLayout: RelativeLayout = itemView.findViewById(R.id.myLayout)
        val otherLayout: RelativeLayout = itemView.findViewById(R.id.otherLayout)
        val time: TextView = itemView.findViewById(R.id.time)
        val otherTime: TextView = itemView.findViewById(R.id.otherTime)
    }
}
