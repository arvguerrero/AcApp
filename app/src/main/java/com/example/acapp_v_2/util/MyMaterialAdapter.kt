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
import com.example.acapp_v_2.models.Material
import com.example.acapp_v_2.models.Product

class MyMaterialAdapter(val context: Context, private val materialList : ArrayList<Material>) : RecyclerView.Adapter<MyMaterialAdapter.MyViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMaterialAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.material_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val material : Material = materialList[position]
        holder.rvName.text = material.materialName
        holder.rvMore.setOnClickListener { view ->
            if (context is InventoryActivity) {
                context.materialInfoDialog(material)
            }
        }
    }


    override fun getItemCount(): Int {
        return materialList.size
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rvName : TextView = itemView.findViewById(R.id.rvName)
        val rvMore: ImageView = itemView.findViewById(R.id.rvMore)
    }
}