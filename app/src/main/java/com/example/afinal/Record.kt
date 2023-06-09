package com.example.afinal

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.*
import java.io.File
import java.io.IOException

data class RequestBody(val username: String, val filename: String)
data class Response(val message: String, val success: Boolean)

// this represents record fragment
class Record : Fragment() {

    private val PERMISSION_RECORD_AUDIO = android.Manifest.permission.RECORD_AUDIO
    private val PERMISSION_WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val REQUEST_PERMISSIONS = 123

    private lateinit var ButtonRecord: Button
    private lateinit var TVRecord: TextView
    private lateinit var recorder: MediaRecorder
    private lateinit var fileName: String
    private lateinit var Storage: StorageReference
    private var isRecording = false;
    private lateinit var runnable: Runnable;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_record, container, false)
        ButtonRecord = v.findViewById(R.id.button_record_record)
        TVRecord = v.findViewById(R.id.tv_record)
        Storage = FirebaseStorage.getInstance().reference

        // recording will be stored here
        fileName = Environment.getExternalStorageDirectory().absolutePath + "/heart_sound.mp3"

        ButtonRecord.setOnClickListener(View.OnClickListener {
                // if app is not recording, then check permission and start recording
                if (!isRecording) {
                    if(checkPermissions()){
                        startRecording()
                        TVRecord.setText("Press button to stop recording")
                        TVRecord.setTextColor(getResources().getColor(R.color.colorPrimaryDark))
                        ButtonRecord.setActivated(true)
                    }
                } else {
                    // if app is recording, then stop recording
                    stopRecording()
                    removeAutoStop()
                    ButtonRecord.setActivated(false)
                    ButtonRecord.setEnabled(false)
                }
        })
        return v
    }

    private fun checkPermissions(): Boolean {
        val recordAudioPermission = ContextCompat.checkSelfPermission(
            requireActivity(),
            PERMISSION_RECORD_AUDIO
        )
        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
            requireActivity(),
            PERMISSION_WRITE_EXTERNAL_STORAGE
        )

        val permissions = ArrayList<String>()

        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(PERMISSION_WRITE_EXTERNAL_STORAGE)
        }

        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(PERMISSION_RECORD_AUDIO)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions.toTypedArray(),
                REQUEST_PERMISSIONS
            )
            return false
        }

        return true
    }

    private fun startRecording() {

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            setOutputFile(File(fileName).absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                setAutoStop();
            } catch (e: IOException) {
                Toast.makeText(requireActivity(), "Recording failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        recorder!!.stop()
        recorder!!.release()
        //recorder = null
        TVRecord!!.text = "Uploading Audio..."
        uploadAudio()
        isRecording = false
    }

    // this will by default stop recording if someone will not stop it
    private fun setAutoStop(){
        val handler = Handler(Looper.getMainLooper())

        runnable = Runnable {
            if(isRecording){
                stopRecording()
                vibrateDevice(requireContext());
                ButtonRecord.setActivated(false)
                ButtonRecord.setEnabled(false)
            }
        }

        val delayInMillis: Long = 10000
        handler.postDelayed(runnable, delayInMillis)
    }

    private fun removeAutoStop(){
        val handler = Handler(Looper.getMainLooper())

        handler.removeCallbacks(runnable)
    }

    // it will upload audio on firebase storage
    private fun uploadAudio() {
        val userId = getUserUId();
        if(userId != ""){
            val fName = getUserUId() + System.currentTimeMillis().toString() + ".mp3";
            val filepath = Storage!!.child("Audio").child(fName)
            val uri = Uri.fromFile(File(fileName))
            filepath.putFile(uri).addOnSuccessListener {
                // on successful upload of audio on Firebase
                // make api call on Flask server to process the audio
                checkFile(getUserUId(), fName)
            }
        }
        else {
            TVRecord!!.text = "There is something wrong"
            TVRecord!!.setTextColor(Color.RED)
        }
    }

    // vibrate device when recording will be automatically stopped
    fun vibrateDevice(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For API 26 and above
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        } else {
            // For API 25 and below
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    // make api call on Flask server to process the audio file
    private fun checkFile(username: String, filename: String){
        val requestBody = RequestBody(username, filename)

        apiService.post(requestBody).enqueue(object : retrofit2.Callback<Response> {
            override fun onResponse(call: retrofit2.Call<Response>, response: retrofit2.Response<Response>) {

                if (response.isSuccessful && response.body()?.success == true) {
                    // Handle successful response
                    Log.d("MESSAGE","success")

                    TVRecord!!.text = "Upload finished! You'll get a notification about the outcome shortly, usually within half minute."
//                    TVRecord!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

                    TVRecord!!.setTextColor(getResources().getColor(R.color.colorAccent))

                } else {
                    // Handle error
                    TVRecord!!.text = "There is something wrong"
                    TVRecord!!.setTextColor(Color.RED)

                }
                ButtonRecord!!.isEnabled = true

            }

            override fun onFailure(call: retrofit2.Call<Response>, t: Throwable) {
                TVRecord!!.text = "There is something wrong"
                TVRecord!!.setTextColor(Color.RED)
                ButtonRecord!!.isEnabled = true

            }
        })
    }

}