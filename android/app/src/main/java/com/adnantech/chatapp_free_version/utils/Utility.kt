package com.adnantech.chatapp_free_version.utils

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import com.adnantech.chatapp_free_version.models.Message
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object Utility {

    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="

    fun decryptMessage(item: Message): String {
        var decryptedMessage: String = ""
        try {
            val ivParameterSpecDecrypt =
                IvParameterSpec(Base64.decode(item.iv, Base64.URL_SAFE))
            val factoryDecrypt = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val specDecrypt = PBEKeySpec(
                secretKey.toCharArray(),
                Base64.decode(salt, Base64.URL_SAFE),
                10000,
                256
            )
            val tmpDecrypt = factoryDecrypt.generateSecret(specDecrypt)
            val secretKeyDecrypt = SecretKeySpec(tmpDecrypt.encoded, "AES")

            val cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipherDecrypt.init(
                Cipher.DECRYPT_MODE,
                secretKeyDecrypt,
                ivParameterSpecDecrypt
            )
            decryptedMessage =
                String(cipherDecrypt.doFinal(Base64.decode(item.message, Base64.URL_SAFE)))
        } catch (exp: Exception) {
            exp.printStackTrace()
            Log.i("mylog", exp.message.toString())
        }

        return decryptedMessage
    }

    fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
        val returnCursor: Cursor = contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()

        return name
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }
    }

    fun showAlert(
        context: Context,
        title: String = "",
        message: String = "",
        onYes: Runnable? = null,
        onNo: Runnable? = null
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(android.R.string.yes) { dialog, which ->
            onYes?.run()
        }

        alertDialogBuilder.setNegativeButton(android.R.string.no) { dialog, which ->
            onNo?.run()
        }
        alertDialogBuilder.show()
    }
}
