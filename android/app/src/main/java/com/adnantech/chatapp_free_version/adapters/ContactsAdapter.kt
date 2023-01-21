package com.adnantech.chatapp_free_version.adapters

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.adnantech.chatapp.utils.FetchImageFromInternet
import com.adnantech.chatapp_free_version.R
import com.adnantech.chatapp_free_version.interfaces.RVInterface
import com.adnantech.chatapp_free_version.models.UserContact
import java.util.*


class ContactsAdapter(
    private var contacts: List<UserContact>,
    private var RVInterface: RVInterface
) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_contact, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val contact: UserContact = contacts[position]

        // sets the text to the textview from our itemHolder class
        holder.name.text = contact.name
        holder.phone.text = contact.phone

//        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
//        val time = df.format(contact.updatedAt)

        if (contact.updatedAt == 0L) {
            holder.updatedAt.text = ""
        } else {
            holder.updatedAt.text = DateUtils.getRelativeTimeSpanString(
                contact.updatedAt,
                Date().time,
                0L,
                DateUtils.FORMAT_ABBREV_ALL
            )
        }

        holder.unread.isVisible = (contact.hasUnreadMessage == 1)

        if (contact.image.isNotEmpty()) {
            FetchImageFromInternet(holder.image).execute(contact.image)
        }

        holder.itemView.setOnClickListener {
            RVInterface.onClick(holder.itemView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setContacts(contacts: ArrayList<UserContact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return contacts.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val phone: TextView = itemView.findViewById(R.id.phone)
        val image: ImageView = itemView.findViewById(R.id.image)
        val unread: TextView = itemView.findViewById(R.id.unread)
        val updatedAt: TextView = itemView.findViewById(R.id.updatedAt)
    }
}
