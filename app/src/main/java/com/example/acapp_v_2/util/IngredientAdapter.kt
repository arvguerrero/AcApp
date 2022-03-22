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
import com.example.acapp_v_2.models.Ingredients

class IngredientAdapter(val context: Context, private val ingredientList : ArrayList<Ingredients>) : RecyclerView.Adapter<IngredientAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val ingredient : Ingredients = ingredientList[position]
        holder.gvName.text = ingredient.ingredientName
        holder.gvQuantity.text = ingredient.quantity
        holder.gvDelete.setOnClickListener { view ->
            if (context is InventoryActivity) {
                context.ingredientInfoDialog(ingredient)
            }
        }
    }


    override fun getItemCount(): Int {
        return ingredientList.size
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val gvName : TextView = itemView.findViewById(R.id.gvName)
        val gvQuantity: TextView = itemView.findViewById(R.id.gvQuantity)
        val gvDelete: ImageView = itemView.findViewById(R.id.gvDelete)
    }
}