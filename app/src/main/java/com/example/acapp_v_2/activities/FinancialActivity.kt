package com.example.acapp_v_2.activities

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
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
import kotlinx.android.synthetic.main.expense_popup.view.*
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
        this.overridePendingTransition(0,0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        }

        val reload = findViewById<View>(R.id.reload)
        reload.setOnClickListener {
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "FinancialActivity")
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

        var totalExpense = 0.0
        var totalIncome = 0.0
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

        val expense = findViewById<TextView>(R.id.addExpense)
        expense.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.expense_popup, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()
            mDialogView.saveBtn.setOnClickListener {
                val expenseName =
                    mDialogView.findViewById<EditText>(R.id.expenseName).text.toString()
                val value = mDialogView.findViewById<EditText>(R.id.value).text.toString()
                val uid = auth.currentUser!!.uid
                var number = 0
                if (expenseName != "" && value != "") {
                    db.collection("users").document(uid).collection("expenses").get()
                        .addOnSuccessListener { documents ->
                            number = documents.size()
                            var item = number.toString()
                            for (document in documents) {
                                if (document.getString("item") == item) {
                                    number += 1
                                    item = number.toString()
                                    val expenses = HashMap<String, Any>()
                                    expenses["cost"] = value
                                    expenses["name"] = expenseName
                                    expenses["uid"] = uid
                                    expenses["item"] = item
                                    expenses["date"] = FieldValue.serverTimestamp()
                                    db.collection("users").document(uid).collection("expenses")
                                        .document(item)
                                        .set(expenses)
                                }
                            }
                            val expenses = HashMap<String, Any>()
                            expenses["cost"] = value
                            expenses["name"] = expenseName
                            expenses["uid"] = uid
                            expenses["item"] = item
                            expenses["date"] = FieldValue.serverTimestamp()
                            db.collection("users").document(uid).collection("expenses")
                                .document(item)
                                .set(expenses)
                        }
                    mAlertDialog.dismiss()
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

                    var totalExpense = 0.0
                    var totalIncome = 0.0
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
                                .update(
                                    mapOf(
                                        "expenses" to cost
                                    )
                                )
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
                                .update(
                                    mapOf(
                                        "income" to income
                                    )
                                )
                        }
                        .addOnFailureListener {
                            Log.w(ContentValues.TAG, "Error getting documents:")
                        }
                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("activity", "FinancialActivity")
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }else{
                    Toast.makeText(
                        this,
                        "Incomplete expense details.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            mDialogView.cancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
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
}
