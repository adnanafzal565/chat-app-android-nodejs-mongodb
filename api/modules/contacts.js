const express = require("express")
const auth = require("./auth")
const fileSystem = require("fs")
 
module.exports = {
 
    init: function (app) {
        const router = express.Router()

        router.post("/fetch", auth, async function (request, result) {
            const user = request.user
            let contacts = user.contacts || []

            contacts.sort(function(a, b) {
                const keyA = typeof a.updatedAt === "undefined" ? 0 : new Date(a.updatedAt)
                const keyB = typeof b.updatedAt === "undefined" ? 0 : new Date(b.updatedAt)

                if (keyA < keyB) return 1
                if (keyA > keyB) return -1
                return 0
            })

            for (let a = 0; a < contacts.length; a++) {
                const exists = await fileSystem.existsSync("uploads/profiles/" + contacts[a]._id.toString() + ".png")
                contacts[a].image = ""

                if (exists) {
                    contacts[a].image = global.apiURL + "/uploads/profiles/" + contacts[a]._id.toString() + ".png"
                }
            }
            
            result.json({
                status: "success",
                message: "Contact has been fetched.",
                contacts: contacts
            })
        })

        router.post("/saveMultiple", auth, async function (request, result) {
            const user = request.user
            const contacts = JSON.parse(request.fields.contacts)

            if (user.contacts.length > 0) {
                result.json({
                    status: "success",
                    message: "Contact has been saved."
                })
                
                return
            }

            const contactPhones = []
            for (let a = 0; a < contacts.length; a++) {
                contactPhones.push(contacts[a].phone.replace(
                    new RegExp(" ", "g"), ""
                ))
            }

            const users = await db.collection("users").find({
                phone: {
                    $in: contactPhones
                }
            }).toArray()

            const toSaveContacts = []
            for (let a = 0; a < users.length; a++) {
                toSaveContacts.push({
                    _id: users[a]._id,
                    name: users[a].name,
                    phone: users[a].phone,
                    hasUnreadMessage: 0,
                    updatedAt: 0
                })
            }

            await global.db.collection("users").findOneAndUpdate({
                _id: user._id
            }, {
                $set: {
                    contacts: toSaveContacts
                }
            })
 
            result.json({
                status: "success",
                message: "Contact has been saved."
            })
        })
 
        app.use("/contacts", router)
    }
}