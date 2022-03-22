package com.example.acapp_v_2.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.acapp_v_2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.logout_dialog.view.*

class HomeActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101
    private var notificationText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        overridePendingTransition(0,0)


        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        val dashboard = findViewById<Button>(R.id.btn_dashboard)
        dashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val inventory = findViewById<Button>(R.id.btn_inventory)
        inventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }

        val financial = findViewById<Button>(R.id.btn_financials)
        financial.setOnClickListener {
            val intent = Intent(this, FinancialActivity::class.java)
            startActivity(intent)
        }

        val profile = findViewById<Button>(R.id.btn_profile)
        profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val logout = findViewById<TextView>(R.id.txt_logout)
        logout.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.logout_dialog, null)
            val mBuilder = android.app.AlertDialog.Builder(this)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()
            mDialogView.logoutBtn.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            mDialogView.cancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
        var totalExpense = 0.0
        var totalIncome = 0.0
        var ctr = 0
        val uid = auth.currentUser!!.uid
        createNotificationChannel()

        db.collection("users").document(uid).collection("materials")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    if(document != null){
                        val threshold = document.getString("thresholdLevel")!!.toInt()
                        val stock = document.getString("stockLevel")!!.toInt()
                        if(threshold > stock){
                            ctr += 1
                            val count = ctr.toString()
                            notificationText = "$count item/s went below their threshold level."
                            sendNotification()
                        }
                    }
                }
            }


        db.collection("users").document(uid).collection("expenses")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val cost = document.getString("cost")
                        totalExpense += cost!!.toDouble()
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                val cost = totalExpense.toString()
                db.collection("users").document(uid)
                    .update(mapOf(
                        "expenses" to cost
                    ))
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }

        db.collection("users").document(uid).collection("income")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val profit = document.getString("profit")
                        totalIncome += profit!!.toDouble()
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                val income = totalIncome.toString()
                db.collection("users").document(uid)
                    .update(mapOf(
                        "income" to income
                    ))
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification(){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Threshold Level Reached!")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }
}