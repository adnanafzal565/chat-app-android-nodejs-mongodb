package com.adnantech.chatapp_free_version.models

class Message {
    var _id: String = ""
    var message: String = ""
    var iv: String = ""
    var audioPath: String = ""
    var sender: User = User()
    var receiver: User = User()
    var extension: String = ""
    var type: String = ""
    var attachment: String = ""
    var isRead: Boolean = false
    var attachmentName: String = ""
    var createdAt: Long = 0
}
