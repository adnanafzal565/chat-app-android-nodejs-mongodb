const express = require("express")
const app = express()

const http = require("http").createServer(app)

// Add headers before the routes are defined
app.use(function (req, res, next) {
 
    // Website you wish to allow to connect
    res.setHeader("Access-Control-Allow-Origin", "*")
 
    // Request methods you wish to allow
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE")
 
    // Request headers you wish to allow
    res.setHeader("Access-Control-Allow-Headers", "X-Requested-With,content-type,Authorization")
 
    // Set to true if you need the website to include cookies in the requests sent
    // to the API (e.g. in case you use sessions)
    res.setHeader("Access-Control-Allow-Credentials", true)
 
    // Pass to next layer of middleware
    next()
})

// module required for parsing FormData values
const expressFormidable = require("express-formidable")
 
// setting the middleware
app.use(expressFormidable())

// module required for encrypting the passwords
// and verify the password as well
const bcryptjs = require("bcryptjs")

// sockets are used for realtime communication
const socketIO = require("socket.io")(http, {
    cors: {
        origin: "*"
    }
})

const mongodb = require("mongodb")
const MongoClient = mongodb.MongoClient
const ObjectId = mongodb.ObjectId
const fileSystem = require("fs")

// JWT used for authentication
const jwt = require("jsonwebtoken")

// secret JWT key
global.jwtSecret = "jwtSecret1234567890"

const auth = require("./modules/auth")
const contacts = require("./modules/contacts")
const chats = require("./modules/chats")

app.use("/uploads", express.static(__dirname + "/uploads"))

const users = []
global.apiURL = "http://192.168.8.100:3000"

const port = process.env.PORT || 3000
http.listen(port, function () {
	console.log("Server started: " + port)

	// connect with database
	MongoClient.connect("mongodb://localhost:27017", function (error, client) {
		global.db = client.db("chat_app_free_version")
	    console.log("Database connected")

	    contacts.init(app)
	    chats.init(app)

		socketIO.on("connection", function (socket) {
			socket.on("connected", function (_id) {
				users[_id] = socket.id
			})

			socket.on("allMessagesRead", function (receiverID, myID) {
				socketIO.to(users[receiverID]).emit("allMessagesRead-" + myID, "1")
			})

			socket.on("messageRead", async function (message) {
				message = JSON.parse(message)

				await global.db.collection("users").findOneAndUpdate({
                    $and: [{
                        _id: ObjectId(message.receiver._id)
                    }, {
                        "contacts._id": ObjectId(message.sender._id)
                    }]
                }, {
                    $set: {
                        "contacts.$.hasUnreadMessage": 0
                    }
                })

                await db.collection("messages").findOneAndUpdate({
                    _id: ObjectId(message._id)
                }, {
                    $set: {
                        isRead: true
                    }
                })

				socketIO.to(users[message.sender._id]).emit("messageRead", JSON.stringify(message))
			})

			socket.on("newMessage", function (message) {
				message = JSON.parse(message)
				socketIO.to(users[message.receiver._id]).emit("newMessage", JSON.stringify(message))
			})
		})

		/*app.get("/", async function (request, result) {
			require("tesseract.js").recognize(
				this.apiURL + "/uploads/1672902104426.png",
				"eng",
				{
					logger: function (log) {
						console.log({
							log: log
						})
					}
				}
			).then(function (data) {
				console.log(data.data.text)
			})
			result.send("Hello world.")
		})*/

	    app.post("/saveFCMToken", auth, async function (request, result) {
		    const user = request.user
		    const token = request.fields.token

		    if (!token) {
		    	result.json({
			        status: "error",
			        message: "Please fill all fields."
			    })
			    return
		    }
		 
		    // update JWT of user in database
		    await global.db.collection("users").findOneAndUpdate({
		        _id: user._id
		    }, {
		        $set: {
		            fcmToken: token
		        }
		    })
		 
		    result.json({
		        status: "success",
		        message: "Token saved successfully."
		    })
		})

	    // route for logout request
		app.post("/logout", auth, async function (request, result) {
		    const user = request.user
		 
		    // update JWT of user in database
		    await global.db.collection("users").findOneAndUpdate({
		        _id: user._id
		    }, {
		        $set: {
		            accessToken: ""
		        }
		    })
		 
		    result.json({
		        status: "success",
		        message: "Logout successfully."
		    })
		})

		app.post("/updateProfile", auth, async function (request, result) {
		    const user = request.user
			const name = request.fields.name ?? ""
			const base64 = request.fields.base64 ?? ""

			if (!name) {
				result.json({
					status: "error",
					message: "Please enter all fields."
				})

				return
			}

			if (base64) {
				const filePath = "uploads/profiles/" + user._id + ".png"
				fileSystem.writeFile(filePath, base64, "base64", function (error) {
					if (error) {
						console.log(error)
					}
				})

				await db.collection("users").findOneAndUpdate({
					_id: user._id
				}, {
					$set: {
						name: name,
						image: filePath
					}
				})
			} else {
				await db.collection("users").findOneAndUpdate({
					_id: user._id
				}, {
					$set: {
						name: name
					}
				})
			}
		 
		    result.json({
		        status: "success",
		        message: "Profile has been updated."
		    })
		})

		app.post("/fetchUser", auth, async function (request, result) {
			const user = request.user
			const _id = request.fields._id ?? ""

			if (!_id) {
				result.json({
					status: "error",
					message: "Required parameter _id is missing."
				})

				return
			}

			const userObj = await db.collection("users").findOne({
				_id: ObjectId(_id)
			})

			if (userObj == null) {
				result.json({
					status: "error",
					message: "User does not exists."
				})

				return
			}

			const exists = await fileSystem.existsSync(userObj.image)
			if (exists) {
				userObj.image = apiURL + "/" + userObj.image
			} else {
				userObj.image = ""
			}
		 
		    result.json({
		        status: "success",
		        message: "Data has been fetched.",
		        user: {
					_id: userObj._id,
					name: userObj.name,
					phone: userObj.phone,
					image: userObj.image,
					createdAt: userObj.createdAt
				},
				me: user
		    })
		})

	    app.post("/getUser", auth, async function (request, result) {
		    const user = request.user
		 
		    result.json({
		        status: "success",
		        message: "Data has been fetched.",
		        user: user
		    })
		})

	    // route for login requests
		app.post("/login", async function (request, result) {
		    // get values from login form
		    const phone = request.fields.phone
		    const password = request.fields.password
		 
		    // check if phone exists
		    const user = await global.db.collection("users").findOne({
		        phone: phone
		    })
		 
		    if (user == null) {
		        result.json({
		            status: "error",
		            message: "Phone does not exists."
		        })
		        return
		    }

		    const isVerify = await bcryptjs.compareSync(password, user.password)
		 
		    if (isVerify) {
	            // generate JWT of user
	            const accessToken = jwt.sign({
	                userId: user._id.toString()
	            }, jwtSecret)
	 
	            // update JWT of user in database
	            await global.db.collection("users").findOneAndUpdate({
	                _id: user._id
	            }, {
	                $set: {
	                    accessToken: accessToken
	                }
	            })
	 
	            result.json({
	                status: "success",
	                message: "Login successfully.",
	                accessToken: accessToken,
					user: {
						_id: user._id,
						name: user.name,
						phone: user.phone,
						image: user.image
					}
	            })
	 
	            return
	        }
	 
	        result.json({
	            status: "error",
	            message: "Password is not correct."
	        })
		})
	 
	    app.post("/register", async function (request, result) {
	        const name = request.fields.name
	        const phone = request.fields.phone
	        const password = request.fields.password
	        const createdAt = new Date().getTime()
	 
	        if (!name || !phone || !password) {
	            result.json({
	                status: "error",
	                message: "Please enter all values."
	            })
	            return
	        }
	 
	        // check if email already exists
	        const user = await global.db.collection("users").findOne({
	            phone: phone
	        })
	 
	        if (user != null) {
	            result.json({
	                status: "error",
	                message: "Phone already exists."
	            })
	            return
	        }

	        const salt = await bcryptjs.genSaltSync(10)
	        const hash = await bcryptjs.hashSync(password, salt)
	 
	        // insert in database
            await global.db.collection("users").insertOne({
                name: name,
                phone: phone,
                password: hash,
                accessToken: "",
                fcmToken: "",
                contacts: [],
                createdAt: createdAt
            })
 
            result.json({
                status: "success",
                message: "Account has been created. Please login now."
            })
	    })
	})
})