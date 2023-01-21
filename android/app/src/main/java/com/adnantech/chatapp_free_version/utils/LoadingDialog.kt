package com.adnantech.chatapp_free_version.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.adnantech.chatapp_free_version.R

class LoadingDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.loading_dialog)
    }
}
