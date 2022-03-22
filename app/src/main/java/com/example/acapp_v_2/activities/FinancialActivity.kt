package com.example.acapp_v_2.activities

import android.content.ContentValues.TAG
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.models.Expense
import com.example.acapp_v_2.models.Financials
import com.example.acapp_v_2.models.Product
import com.example.acapp_v_2.util.ExpenseAdapter
import com.example.acapp_v_2.util.MyAdapter
import com.example.acapp_v_2.util.ProfitAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_financial.*
import org.w3c.dom.Text
import java.lang.Math.log

class FinancialActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profitRecyclerView: RecyclerView
    private lateinit var profitArrayList: ArrayList<Financials>
    private lateinit var profitAdapter: ProfitAdapter
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var expenseArrayList: ArrayList<Expense>
    private lateinit var expenseAdapter: ExpenseAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial)
        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        profitRecyclerView = findViewById(R.id.revenue)
        profitRecyclerView.layoutManager = LinearLayoutManager(this)
        profitRecyclerView.setHasFixedSize(true)
        profitArrayList = arrayListOf()
        profitAdapter = ProfitAdapter(this, profitArrayList)
        profitRecyclerView.adapter = profitAdapter
        AddIncomeListener()

        expenseRecyclerView = findViewById(R.id.cost)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseRecyclerView.setHasFixedSize(true)
        expenseArrayList = arrayListOf()
        expenseAdapter = ExpenseAdapter(this, expenseArrayList)
        expenseRecyclerView.adapter = expenseAdapter
        AddExpenseListener()
        var total = 0.0




        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val income = document.getString("income")
                    val expenses = document.getString("expenses")
                    total = (income!!.toDouble() - expenses!!.toDouble())
                    profitNumber.text = total.toString()
                    expensesNumber.text = expenses
                    incomeNumber.text = income
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }
    private fun AddIncomeListener() {
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
        db.collection("users").document(uid).collection("expenses")
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
}
