package com.example.swipe2back

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class ListAdapter(private val onClickListener:(position:Int,v:View)->Unit): RecyclerView.Adapter<ListAdapter.ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val v:View = LayoutInflater.from(parent.context).inflate(R.layout.it_list_item,parent,false)
        return ListViewHolder(v)
    }

    override fun getItemCount() = 19

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.tvName.text = "Username $position"
        holder.itemView.setOnClickListener {
            onClickListener.invoke(position,it)
        }
    }

    class ListViewHolder(itemView: View) : ViewHolder(itemView) {

        var tvName: TextView

        init {
            tvName = itemView.findViewById(R.id.tv_usrname)
        }


    }
}