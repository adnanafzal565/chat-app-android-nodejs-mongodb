const express = require("express")
const ObjectId = require("mongodb").ObjectId

const auth = require("./auth")
const fileSystem = require("fs")

// const crypto = require('crypto')
// const algorithm = 'aes-256-cbc' // Using AES encryption
const key = "android-chat-app-kotlinnodemongo" // must be of 32 characters

var CryptoJS = require("crypto-js")

const admin = require("firebase-admin")
var fcm = require('fcm-notification')
var serviceAccount = require("../chat-app-6302f-firebase-adminsdk-y8ygk-d2b015c76c.json")
const certPath = admin.credential.cert(serviceAccount)
var FCM = new fcm(certPath)

module.exports = {

    decrypt: function (text) {
        const bytes  = CryptoJS.AES.decrypt(text, key)
        const originalText = bytes.toString(CryptoJS.enc.Utf8)

        return originalText
    },

    encrypt: function (text) {
        return CryptoJS.AES.encrypt(text, key).toString()
    },

    init: function (app) {
        const self = this
        const router = express.Router()

        router.post("/fetch", auth, async function (request, result) {
            const user = request.user
            const userId = request.fields.userId
            const type = request.fields.type ?? ""
            const page = request.fields.page ?? 1
            const limit = 30
         
            if (!userId) {
                result.json({
                    status: "error",
                    message: "Please enter all fields."
                })
                return
            }

            let receiver = await global.db.collection("users").findOne({
                _id: ObjectId(userId)
            })
         
            if (receiver == null) {
                result.json({
                    status: "error",
                    message: "The receiver is not a member of this app."
                })
                return
            }

            let condition = {
                $or: [{
                    "sender._id": user._id,
                    "receiver._id": receiver._id
                }, {
                    "sender._id": receiver._id,
                    "receiver._id": user._id
                }]
            }
         
            const messages = await global.db.collection("messages").find(condition)
                .sort({"createdAt": -1})
                .skip((page - 1) * limit)
                .limit(limit)
                .toArray()
         
            let data = []
            const messageIds = []
            for (let a = 0; a < messages.length; a++) {
                messageIds.push(messages[a]._id)

                const obj = {
                    _id: messages[a]._id.toString(),
                    message: self.decrypt(messages[a].message),
                    sender: {
                        _id: messages[a].sender._id,
                        phone: messages[a].sender.phone,
                        name: messages[a].sender.name
                    },
                    receiver: {
                        _id: messages[a].receiver._id,
                        phone: messages[a].receiver.phone,
                        name: messages[a].receiver.name
                    },
                    type: messages[a].type,
                    createdAt: messages[a].createdAt
                }

                data.push(obj)
            }

            data = data.reverse()

            await global.db.collection("users").findOneAndUpdate({
                $and: [{
                    _id: user._id
                }, {
                    "contacts._id": ObjectId(userId)
                }]
            }, {
                $set: {
                    "contacts.$.hasUnreadMessage": 0
                }
            })

            const exists = await fileSystem.existsSync(receiver.image)
            if (exists) {
                receiver.image = global.apiURL + "/" + receiver.image
            } else {
                receiver.image = ""
            }

            result.json({
                status: "success",
                message: "Messages has been fetched.",
                messages: data,
                user: user,
                receiver: {
                    _id: receiver._id,
                    name: receiver.name,
                    phone: receiver.phone,
                    image: receiver.image
                }
            })
        })

        router.post("/send", auth, async function (request, result) {
            const user = request.user
            const userId = request.fields.userId
            const message = request.fields.message ?? ""

            const _id = request.fields._id ?? ""
            const createdAt = new Date().getTime()

            if (!userId) {
                result.json({
                    status: "error",
                    message: "Please enter all fields."
                })
                return
            }
         
            // Text send to encrypt function
            const hw = self.encrypt(message)
            let receiver = await global.db.collection("users").findOne({
                _id: ObjectId(userId)
            })
         
            if (receiver == null) {
                result.json({
                    status: "error",
                    message: "The receiver is not a member of this app."
                })
                return
            }

            let receiverObj = {
                _id: receiver._id,
                name: receiver.name,
                phone: receiver.phone
            }
         
            const messageObj = {
                message: hw,
                sender: {
                    _id: user._id,
                    name: user.name,
                    phone: user.phone
                },
                receiver: receiverObj,
                createdAt: createdAt
            }
            const insertedDocument = await global.db.collection("messages").insertOne(messageObj)

            messageObj._id = insertedDocument.insertedId
            messageObj.message = message

            if (receiver.fcmToken) {
                try {
                    let message = {
                        android: {
                            data: {
                                title: "New message",
                                message: "New message has been received.",
                                messageText: messageObj.message,
                                name: user.name,
                                phone: user.phone,
                                _id: user._id.toString(),
                                receiver: JSON.stringify(receiverObj),
                                sender: JSON.stringify({
                                    _id: user._id,
                                    name: user.name,
                                    phone: user.phone
                                }),
                                createdAt: createdAt.toString()
                            }
                        },
                        token: receiver.fcmToken
                    }

                    FCM.send(message, function(err, resp) {
                        if (err) {
                            console.error(err)
                        } else {
                            console.log('Successfully sent notification: ' + receiver.fcmToken)
                        }
                    })
                } catch (err) {
                    console.error(err)
                }
            }

            // update my document
            await global.db.collection("users").findOneAndUpdate({
                $and: [{
                    _id: user._id
                }, {
                    "contacts._id": receiver._id
                }]
            }, {
                $set: {
                    "contacts.$.updatedAt": new Date().getTime()
                }
            })

            // update receiver's document
            await global.db.collection("users").findOneAndUpdate({
                $and: [{
                    _id: receiver._id
                }, {
                    "contacts._id": user._id
                }]
            }, {
                $set: {
                    "contacts.$.hasUnreadMessage": 1,
                    "contacts.$.updatedAt": new Date().getTime()
                }
            })
         
            result.json({
                status: "success",
                message: "Message has been sent.",
                messageObj: messageObj,
                user: user
            })
        })
 
        app.use("/chats", router)
    }
}