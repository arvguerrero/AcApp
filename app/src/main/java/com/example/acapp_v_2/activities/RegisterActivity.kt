package com.example.acapp_v_2.activities

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.acapp_v_2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlin.collections.Map as Map1

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ref: DocumentReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        btnRegister.setOnClickListener{
            signUpUser()
        }

        alreadyHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun signUpUser() {
        val firstName = inputFirstName.text.toString().trim()
        val lastName = inputLastName.text.toString().trim()
        val businessName = inputBusinessName.text.toString().trim()
        val email = inputEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        if(email.isEmpty()){
            inputEmail.error = "Please enter a valid email address."
            inputEmail.requestFocus()
            return
        } else if (password.isEmpty()){
            loginPassword.error = "Please enter a password"
            loginPassword.requestFocus()
            return
        } else if (loginPassword.text.toString() != inputConfirmPassword.text.toString()){
            inputConfirmPassword.error = "Passwords do not match"
            inputConfirmPassword.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(inputEmail.text.toString(), inputConfirmPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserFireStore(firstName, lastName, businessName, email)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Sign up failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun saveUserFireStore(firstName: String, lastName: String, businessName: String, email: String){
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser!!.uid
        val user = HashMap<String, Any>()
        user["firstName"] = firstName
        user["lastName"] = lastName
        user["businessName"] = businessName
        user["email"] = email
        user["uid"] = userId
        user["income"] = "0"
        user["expenses"] = "0"
        user["mobile"] = ""


        db.collection("users")
            .document(userId).set(user)
            .addOnSuccessListener {
                Toast.makeText(this@RegisterActivity, "registration successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this@RegisterActivity, "registration failed", Toast.LENGTH_SHORT).show()
            }
    }

/*    fun getCurrentUser(uid: String){
        val db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val uid = auth.currentUser
        val user: MutableMap<String, Any> = HashMap()
        user["uid"] = uid

    }
*/
}