package com.example.smartscheduler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscheduler.Activity.DestinationListLayout
import com.example.smartscheduler.Activity.StartpointListLayout
import com.example.smartscheduler.Database.DestinationInfo
import com.example.smartscheduler.Database.ScheduleInfo

class StartpointSearchAdapter(val itemList : ArrayList<StartpointListLayout>) :
    RecyclerView.Adapter<StartpointSearchAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StartpointSearchAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.startpoint_list_layout, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: StartpointSearchAdapter.ViewHolder, position: Int) {
        holder.name.text = itemList[position].name
        holder.road.text = itemList[position].road
        holder.address.text = itemList[position].address
        // 아이템 클릭 이벤트
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_list_name)
        val road: TextView = itemView.findViewById(R.id.tv_list_road)
        val address: TextView = itemView.findViewById(R.id.tv_list_address)
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener
}



