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
import com.example.acapp_v_2.models.Financials
import com.example.acapp_v_2.models.Product

class ProfitAdapter(val context: Context, private val profitList : ArrayList<Financials>) : RecyclerView.Adapter<ProfitAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfitAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.income_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfitAdapter.MyViewHolder, position: Int) {
        val financials : Financials = profitList[position]
        holder.rvName.text = financials.name
        val date = financials.date.toString()
        //holder.rvDate.text = financials.date
        holder.rvProfit.text = financials.profit
    }


    override fun getItemCount(): Int {
        return profitList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvName : TextView = itemView.findViewById(R.id.rvName)
        //val rvDate: TextView = itemView.findViewById(R.id.rvDate)
        val rvProfit: TextView = itemView.findViewById(R.id.rvProfit)
    }
}