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

class Firebase {

}

fun setToken(token:String){
    val userId = getUserUId()
    if(userId != ""){
        val database = FirebaseDatabase.getInstance().reference
        database.child("messaging-tokens").child(userId).setValue(token)
    }

}

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
//onSuccess: (Map<String, Any>?) -> Unit, onCancel: (DatabaseError) -> Unit
fun getHistory(onSuccess: (MutableList<Map<String, Any>>?) -> Unit, onCancel: (DatabaseError) -> Unit){
    val userId = getUserUId()
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("/history/$userId")

    Log.d("customtag","/history/$userId")

    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            Log.d("customtag","history called 3")

            val map = snapshot.getValue(object : GenericTypeIndicator<Map<String, Map<String, Any>>>() {})
            var data = formatData(map)
            Log.d("CUSTOM===","here")

            onSuccess(data)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("customtag","history called 1")

            // Handle the error here
            onCancel(error)
        }
    })
}

fun formatData(data: Map<String, Map<String, Any>>?): MutableList<Map<String, Any>>? {
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.US)
    sdfDate.timeZone = TimeZone.getDefault()
    sdfTime.timeZone = TimeZone.getDefault()

    val res = mutableListOf<Map<String, Any>>()

    if(data != null){
        for ((key, value: Map<String, Any>) in data) {

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