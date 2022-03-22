package com.example.acapp_v_2.activities

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.admin.AdminHome
import com.example.acapp_v_2.admin.AdminProfitAdapter
import com.example.acapp_v_2.models.Expense
import com.example.acapp_v_2.models.Financials
import com.example.acapp_v_2.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_financial.*
import kotlinx.android.synthetic.main.expense_popup.view.*

class AdminFinancials : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profitRecyclerView: RecyclerView
    private lateinit var profitArrayList: ArrayList<Financials>
    private lateinit var profitAdapter: AdminProfitAdapter
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var expenseArrayList: ArrayList<Expense>
    private lateinit var expenseAdapter: AdminExpenseAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_financials)
        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, AdminHome::class.java)
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        }

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        profitRecyclerView = findViewById(R.id.revenue)
        profitRecyclerView.layoutManager = LinearLayoutManager(this)
        profitRecyclerView.setHasFixedSize(true)
        profitArrayList = arrayListOf()
        profitAdapter = AdminProfitAdapter(this, profitArrayList)
        profitRecyclerView.adapter = profitAdapter
        AddIncomeListener()

        expenseRecyclerView = findViewById(R.id.cost)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseRecyclerView.setHasFixedSize(true)
        expenseArrayList = arrayListOf()
        expenseAdapter = AdminExpenseAdapter(this, expenseArrayList)
        expenseRecyclerView.adapter = expenseAdapter
        AddExpenseListener()
        AddIncomeListener()

        val deleteIncome = findViewById<Button>(R.id.deleteRevenue)
        deleteIncome.setOnClickListener {
            val uid = auth.currentUser!!.uid
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Records")
            builder.setMessage("Are you sure you wants to delete all the records?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                db.collection("users").document(uid).collection("income")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            if (document != null) {
                                val item = document.getString("item").toString()
                                db.collection("users").document(uid).collection("income").document(item).delete()
                            } else {
                                Log.d(ContentValues.TAG, "No such document")
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.w(ContentValues.TAG, "Error getting documents:")
                    }
                dialogInterface.dismiss()
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("activity", "AdminInventory")
                startActivity(intent)
                overridePendingTransition(0,0)
                finish()// Dialog will be dismissed
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        val deleteExpense = findViewById<Button>(R.id.deleteExpenses)
        deleteExpense.setOnClickListener {
            val uid = auth.currentUser!!.uid
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Records")
            builder.setMessage("Are you sure you wants to delete all the records?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                db.collection("users").document(uid).collection("expenses")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            if (document != null) {
                                val item = document.getString("item").toString()
                                db.collection("users").document(uid).collection("expenses").document(item).delete()
                            } else {
                                Log.d(ContentValues.TAG, "No such document")
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.w(ContentValues.TAG, "Error getting documents:")
                    }
                dialogInterface.dismiss()
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("activity", "AdminInventory")
                startActivity(intent)
                overridePendingTransition(0,0)
                finish()// Dialog will be dismissed
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }
    private fun AddIncomeListener() {
        db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("income").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            profitArrayList.add(dc.document.toObject(Financials::class.java))
                        }
                    }
                    profitAdapter.notifyDataSetChanged()
                }
            })
    }
    private fun AddExpenseListener() {
        db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("expenses").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            expenseArrayList.add(dc.document.toObject(Expense::class.java))
                        }
                    }
                    expenseAdapter.notifyDataSetChanged()
                }
            })
    }
    fun expenseInfoDialog(expense: Expense) {
        val item = expense.item.toString()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setMessage("Are you sure you wants to delete this item?")
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            val uid = auth.currentUser!!.uid
            db.collection("users").document(uid).collection("expenses").document(item)
                .delete()
                .addOnSuccessListener {
                    db = FirebaseFirestore.getInstance()
                    val uid = auth.currentUser!!.uid
                    db.collection("users").document(uid).collection("expenses")
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                if (error != null) {
                                    Log.e("Firestore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.REMOVED) {
                                        expenseArrayList.remove(dc.document.toObject(Expense::class.java))
                                    }
                                }
                                expenseAdapter.notifyDataSetChanged()
                            }
                        })
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            dialogInterface.dismiss()
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "AdminFinancials")
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
            // Dialog will be dismissed
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun profitInfoDialog(financials: Financials) {
        val item = financials.item.toString()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setMessage("Are you sure you wants to delete this item?")
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            val uid = auth.currentUser!!.uid
            db.collection("users").document(uid).collection("income").document(item)
                .delete()
                .addOnSuccessListener {
                    db = FirebaseFirestore.getInstance()
                    val uid = auth.currentUser!!.uid
                    db.collection("users").document(uid).collection("income")
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                if (error != null) {
                                    Log.e("Firestore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.REMOVED) {
                                        profitArrayList.remove(dc.document.toObject(Financials::class.java))
                                    }
                                }
                                profitAdapter.notifyDataSetChanged()
                            }
                        })
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            dialogInterface.dismiss()
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "AdminFinancials")
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
            // Dialog will be dismissed
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
