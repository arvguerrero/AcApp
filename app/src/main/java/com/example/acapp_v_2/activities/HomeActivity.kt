package com.example.acapp_v_2.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.example.acapp_v_2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.logout_dialog.view.*

class HomeActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val dashboard = findViewById<Button>(R.id.btn_dashboard)
        dashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
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
    }
}