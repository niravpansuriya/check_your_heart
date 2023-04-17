package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging

class Login : AppCompatActivity() {

    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var ButtonSignIn: Button
    private lateinit var signUpTextView: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthStateListner: AuthStateListener
    private lateinit var googleApiClient: GoogleApiClient
    companion object {
        private const val RC_SIGN_IN = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        mEmail = findViewById(R.id.username)
        mPassword = findViewById(R.id.password)
        ButtonSignIn = findViewById(R.id.button_signin_login)
        signUpTextView = findViewById(R.id.tv_signup)
        var gSignInButton = findViewById<View>(R.id.sign_in_button) as SignInButton

        mAuthStateListner = AuthStateListener {
            val mFirebaseUser = mAuth!!.currentUser
            if (mFirebaseUser != null) {
                Toast.makeText(this@Login, "Login Successful!", Toast.LENGTH_SHORT).show()
                val i = Intent(this@Login, Dashboard::class.java)
                startActivity(i)
                finish()
                return@AuthStateListener
            }
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()


        ButtonSignIn.setOnClickListener(View.OnClickListener {
            val email = mEmail.getText().toString()
            val password = mPassword.getText().toString()
            if(email != "" && password != "")
            {
                mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@Login) { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(
                                this@Login,
                                "Login Unsuccessful! Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            else{
                Toast.makeText(this@Login, "Email or Password can't be empty",Toast.LENGTH_SHORT)
            }
        })

        signUpTextView.setOnClickListener(View.OnClickListener {
            val i = Intent(this, Signup::class.java)
            startActivity(i)
        })

        gSignInButton.setOnClickListener({
            signInWithGoogle()
        })


    }

    // sign in with google
    private fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            if (result?.isSuccess == true) {
                // Google sign-in was successful, authenticate with Firebase
                val account = result?.signInAccount
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                // normal authentication with firebase
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Authentication with Firebase was successful
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT)
                        } else {
                            // Authentication with Firebase failed
                            Toast.makeText(this, "There is something wrong", Toast.LENGTH_SHORT)
                        }
                    }
            } else {
                // Google sign-in failed
                Toast.makeText(this, "There is something wrong", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthStateListner!!)
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(mAuthStateListner!!)
    }
}