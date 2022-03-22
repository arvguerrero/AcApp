package com.example.acapp_v_2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.acapp_v_2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.loginPassword
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        createAcc.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btnlogin.setOnClickListener {
            doLogin()
        }
    }

    private fun doLogin() {
        if(loginFirstName.text.toString().isEmpty()){
            loginFirstName.error = "Please enter a valid email address."
            loginFirstName.requestFocus()
            return
        } else if (loginPassword.text.toString().isEmpty()) {
            loginPassword.error = "Please enter a password"
            loginPassword.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(loginFirstName.text.toString(), loginPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Login failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser : FirebaseUser?){
        if (currentUser != null){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            Toast.makeText(baseContext, "Please login.", Toast.LENGTH_SHORT).show()
        }
    }
}
