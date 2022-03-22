package com.example.acapp_v_2.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.activities.AdminInventory
import com.example.acapp_v_2.activities.InventoryActivity
import com.example.acapp_v_2.models.Product

class MyAdapter(val context: Context, private val productList : ArrayList<Product>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.product_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val product : Product = productList[position]
        holder.productName.text = product.productName
        holder.quantity.text = product.stockLevel
        holder.ivMore.setOnClickListener { view ->
            if (context is InventoryActivity) {
                context.productInfoDialog(product)
            }else if (context is AdminInventory) {
                context.productInfoDialog(product)
            }
        }
    }


    override fun getItemCount(): Int {
        return productList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val productName : TextView = itemView.findViewById(R.id.tvName)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        val quantity : TextView = itemView.findViewById(R.id.tvQuantity)
    }
}