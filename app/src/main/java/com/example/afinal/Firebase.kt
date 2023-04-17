package com.example.afinal

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*

// this class contains static functions which can be
// used in whole project
class Firebase {

}

// it will store the FCM (push notification) token in the firebase
fun setToken(token:String){
    val userId = getUserUId()
    if(userId != ""){
        val database = FirebaseDatabase.getInstance().reference
        database.child("messaging-tokens").child(userId).setValue(token)
    }

}

// it will generate the FCM token and will store it in the firebase
fun establishCloudMessagingConnection(){
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("ERROR", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and store the token as needed
            Log.d("INFO", "FCM registration token: $token")

            setToken(token)
        }
}

// it will return the user's id
fun getUserUId(): String{
    val auth = FirebaseAuth.getInstance()

    val user = auth.currentUser
    if (user != null) {
        // The user is signed in, so you can get the user ID
        val userId = user.uid;
        return userId;
    }
    else return "";
}

// it will give the past record of the user from the firebase
fun getHistory(onSuccess: (MutableList<Map<String, Any>>?) -> Unit, onCancel: (DatabaseError) -> Unit){
    val userId = getUserUId()
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("/history/$userId")

    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            val map = snapshot.getValue(object : GenericTypeIndicator<Map<String, Map<String, Any>>>() {})
            var data = formatData(map)

            onSuccess(data)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle the error here
            onCancel(error)
        }
    })
}

// we are storing data in form of time epoch, this is because user can get
// time based on local region. This function will convert the time epoch to
// user's timezone's date and time
fun formatData(data: Map<String, Map<String, Any>>?): MutableList<Map<String, Any>>? {
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.US)
    sdfDate.timeZone = TimeZone.getDefault()
    sdfTime.timeZone = TimeZone.getDefault()

    val res = mutableListOf<Map<String, Any>>()

    if(data != null){
        var sortedData = data.toList().sortedBy { (_, value) ->
            value["timestamp"] as Long
        }.toMap()
        for ((key, value: Map<String, Any>) in sortedData) {

            val timestamp = value["timestamp"] as Long
            val date = Date(timestamp)
            val dateString = sdfDate.format(date)
            val timeString = sdfTime.format(date)

            res.add(mapOf(
                "result" to data.getValue(key).getValue("result"),
                "date" to dateString,
                "time" to timeString
            ))
        }
        return res
    }
    else return data
}