package com.example.acapp_v_2.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.activities.InventoryActivity
import com.example.acapp_v_2.models.Expense
import com.example.acapp_v_2.models.Financials
import com.example.acapp_v_2.models.Product

class ExpenseAdapter(val context: Context, private val expenseList : ArrayList<Expense>) : RecyclerView.Adapter<ExpenseAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.expense_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseAdapter.MyViewHolder, position: Int) {
        val expense : Expense = expenseList[position]
        holder.evName.text = expense.name
        //holder.rvDate.text = financials.date
        holder.evCost.text = expense.cost
    }


    override fun getItemCount(): Int {
        return expenseList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val evName : TextView = itemView.findViewById(R.id.evName)
        // val rvDate: TextView = itemView.findViewById(R.id.rvDate)
        val evCost: TextView = itemView.findViewById(R.id.evCost)
    }
}