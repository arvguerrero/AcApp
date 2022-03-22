package com.example.acapp_v_2.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.acapp_v_2.R
import com.example.acapp_v_2.util.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.io.IOException
import java.util.*


class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var profilePic: ImageView
    private lateinit var imageUri: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        this.overridePendingTransition(0,0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        }

        val businessName = findViewById<TextView>(R.id.businessName)
        val fName = findViewById<TextView>(R.id.fName)
        val lName = findViewById<TextView>(R.id.lName)
        val bName = findViewById<TextView>(R.id.bName)
        val emailAdd = findViewById<TextView>(R.id.emailAdd)
        val num = findViewById<TextView>(R.id.number)
        val photo = findViewById<ImageView>(R.id.profilePicture)


        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()
        storageRef = storage.getReference()
        val userId = auth.currentUser!!.uid


        val store = storage.reference.child("images/$userId")
        val localfile = File.createTempFile("$userId","jpg")

        store.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            photo.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(this, "Failed to update profile picture",Toast.LENGTH_SHORT).show()
        }

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
                    num.text = document.getString("mobile")
                    emailAdd.isEnabled = false

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        profilePicture.setOnClickListener(this@ProfileActivity)
        btn_save.setOnClickListener(this@ProfileActivity)
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
                    val photo = findViewById<ImageView>(R.id.profilePicture)
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
                    var pd =  ProgressDialog(this);
                    pd.setTitle("Uploading image...");
                    pd.show();
                    val userId = auth.currentUser!!.uid
                    val riversRef: StorageReference = storageRef.child("images/$userId")

                    riversRef.putFile(selectedImageFileUri)
                        .addOnSuccessListener {
                            pd.dismiss()
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                " Image Uploaded.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener {
                            pd.dismiss()
                            Toast.makeText(
                                applicationContext,
                                "Failed to Upload",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }catch(e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@ProfileActivity,
                        "Image selection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun uploadPicture(){
        storageRef = storage.reference

    }
}