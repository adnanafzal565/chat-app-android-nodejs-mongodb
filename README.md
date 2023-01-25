# Android chat app - Kotlin, Node JS, Mongo DB

We created a chat application in native android using Kotlin, Node JS and Mongo DB.

# Screenshots

<p float="left">
    <img src="https://adnan-tech.com/uploads/Welcome.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Register.png" width="250" />
    <img src="https://adnan-tech.com/uploads/android-chat-app-login.png" width="250" />
    <img src="https://adnan-tech.com/uploads/My-profile.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Profile-updated.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Permissions.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Contacts-list.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Chat-activity.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Create-group.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Groups-list.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Group-message.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Share-status-or-story.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Select-contacts-to-add-in-list.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Select-contacts-to-exclude.png" width="250" />
    <img src="https://adnan-tech.com/uploads/Left-menu-drawer-layout.png" width="250" />
</p>

# How to install

- Make sure you have downloaded and installed Node JS and Mongo DB in your system.

- Open command prompt or terminal inside "api" folder and run the following commands one-by-one:
	> npm update
	> npm install -g nodemon
	> nodemon server.js

- Run the following command:
	> ifconfig (mac, linux)
	or
	> ipconfig /all (windows, linux)

- And copy the ipv4 address, it will be something like this:
	> inet 192.168.8.101

- Copy the address after "inet"

- Paste it in "android/app/src/main/java/com/adnantech/chatapp_free_version/utils/MySharedPreference.kt" at line 12

- Also paste it in "api/server.js" variable named "global.apiURL"

- Then goto "https://console.firebase.google.com/" and create a new project.
- Select "android" app
- Select the package as "com.adnantech.chatapp_free_version"
- Download the "google-services.json" file and paste it inside your "android/app" folder.
- Then go to "Project settings"
- And go to "Service accounts" tab.
- Select "Firebase admin SDK" and select "Node.js"
- Click on button "Generate new private key"
- Download the JSON file and paste it inside "api" folder.
- Open "api/modules/chats.js" and set the downloaded JSON file name at line 15.

If you face any problem in setting up, feel free to contact us: support@adnan-tech.com