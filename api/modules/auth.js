const jwt = require("jsonwebtoken")
const ObjectId = require("mongodb").ObjectId
const fileSystem = require("fs")

module.exports = async function (request, result, next) {
    try {
        const accessToken = request.headers.authorization.split(" ")[1]
        const decoded = jwt.verify(accessToken, global.jwtSecret)
        const userId = decoded.userId
 
        const user = await global.db.collection("users").findOne({
            accessToken: accessToken
        })
 
        if (user == null) {
            result.json({
                status: "error",
                message: "User has been logged out."
            })
            return
        }

        const userObj = {
            _id: user._id,
            name: user.name,
            phone: user.phone,
            image: "",
            contacts: user.contacts
        }

        const exists = await fileSystem.existsSync(user.image)
        if (exists) {
            userObj.image = global.apiURL + "/" + user.image
        } else {
            userObj.image = ""
        }

        request.user = userObj
        next()
    } catch (exp) {
        result.json({
            status: "error",
            message: "User has been logged out."
        })
    }
}