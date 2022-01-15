package com.example.instragramview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide


class AdapterView(private val context: Context, private val imagelist: ArrayList<String>) :
    RecyclerView.Adapter<AdapterView.ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.listview, parent, false)
        return ItemViewHolder(itemView)
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Glide.with(context).load(imagelist[position]).into(holder.imagelistview)


    }

    override fun getItemCount(): Int {
        return imagelist.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagelistview: ImageView = itemView.findViewById(R.id.imageview)
    }


}