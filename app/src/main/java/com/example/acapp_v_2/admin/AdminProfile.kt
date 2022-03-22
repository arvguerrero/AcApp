package com.example.acapp_v_2.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.acapp_v_2.R
import com.example.acapp_v_2.admin.AdminHome
import com.example.acapp_v_2.models.User
import com.example.acapp_v_2.util.Constants
import com.google.android.gms.auth.GoogleAuthUtil.getToken
import com.google.android.gms.auth.zzl.getToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_admin_profile.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bName
import kotlinx.android.synthetic.main.activity_profile.btn_save
import kotlinx.android.synthetic.main.activity_profile.fName
import kotlinx.android.synthetic.main.activity_profile.lName
import kotlinx.android.synthetic.main.activity_profile.number
import kotlinx.android.synthetic.main.activity_profile.profilePicture
import java.io.IOException


class AdminProfile : AppCompatActivity(), View.OnClickListener {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)
        this.overridePendingTransition(0,0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, AdminHome::class.java)
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        }

        val businessName = findViewById<TextView>(R.id.businessName)
        val fName = findViewById<TextView>(R.id.fName)
        val lName = findViewById<TextView>(R.id.lName)
        val bName = findViewById<TextView>(R.id.bName)
        val emailAdd = findViewById<TextView>(R.id.emailAdd)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val userId = auth.currentUser!!.uid

        val docRef = db.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    fName.text = document.getString("firstName")
                    lName.text = document.getString("lastName")
                    businessName.text = document.getString("businessName")
                    bName.text = document.getString("businessName")
                    emailAdd.text = document.getString("email")
                    emailAdd.isEnabled = false

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        profilePicture.setOnClickListener(this@AdminProfile)
        btn_save.setOnClickListener(this@AdminProfile)
        btn_delete.setOnClickListener(this@AdminProfile)
    }

    override fun onClick(v: View?) {
        if (v != null){
            when (v.id){
                R.id.profilePicture -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED) {
                        Constants.showImageChooser(this)
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }
                R.id.btn_save -> {
                    val firstName = fName.text.toString()
                    val lastName = lName.text.toString()
                    val business = bName.text.toString()
                    val phone = number.text.toString()
                    val uid = auth.currentUser!!.uid

                    db.collection("users").document(uid)
                        .update(mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "businessName" to business,
                            "mobile" to phone
                        ))
                }
                R.id.btn_delete -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Delete Record")
                    builder.setMessage("Are you sure you wants to delete this item?")
                    builder.setPositiveButton("Yes") { dialogInterface, which ->
                        val uid = auth.currentUser!!.uid
                        db.collection("users").document(uid)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    applicationContext,
                                    "Record deleted successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val user = Firebase.auth.currentUser!!
                                user.delete()
                                    .addOnCompleteListener { task ->
                                        if(task.isComplete) {
                                            Log.d(TAG, "User account deleted.")
                                        }
                                    }
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                        dialogInterface.dismiss()
                    }
                    builder.setNegativeButton("No") { dialogInterface, which ->
                        dialogInterface.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(this, "The storage permission is denied, go to settings.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    val selectedImageFileUri = data.data!!
                    profilePicture.setImageURI(selectedImageFileUri)
                }catch(e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AdminProfile,
                        "Image selection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}