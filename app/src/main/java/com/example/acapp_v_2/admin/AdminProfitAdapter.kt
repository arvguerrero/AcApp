package com.example.acapp_v_2.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.activities.AdminFinancials
import com.example.acapp_v_2.models.Financials

class AdminProfitAdapter(val context: Context, private val profitList : ArrayList<Financials>) : RecyclerView.Adapter<AdminProfitAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.admin_expense_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val financials : Financials = profitList[position]
        holder.rvName.text = financials.name
        holder.rvProfit.text = financials.profit
        holder.rvDelete.setOnClickListener { view ->
            if (context is AdminFinancials) {
                context.profitInfoDialog(financials)
            }
        }
    }


    override fun getItemCount(): Int {
        return profitList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvName : TextView = itemView.findViewById(R.id.evName)
        val rvProfit: TextView = itemView.findViewById(R.id.evCost)
        val rvDelete: ImageView = itemView.findViewById(R.id.evDelete)
     }
}