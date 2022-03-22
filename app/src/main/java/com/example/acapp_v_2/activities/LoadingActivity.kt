package com.example.acapp_v_2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.acapp_v_2.R

class LoadingActivity : AppCompatActivity() {

    private val SPLASH_TIME: Long = 1500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val intent = intent
        val activity = intent.getStringExtra("activity")
        Handler().postDelayed( {
            if (activity == "InventoryActivity"){
                startActivity(Intent (this, InventoryActivity::class.java))
                finish()
            } else if(activity == "FinancialActivity"){
                startActivity(Intent (this, FinancialActivity::class.java))
                finish()
            } else if(activity == "AdminInventory"){
                startActivity(Intent (this, AdminInventory::class.java))
                finish()
            } else if(activity == "AdminFinancials"){
                startActivity(Intent (this, AdminFinancials::class.java))
                finish()
            }
        }, SPLASH_TIME)
    }
}