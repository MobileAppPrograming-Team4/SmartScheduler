package com.example.smartscheduler

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class ScheduleInfoAdapter(context: Context) :
    RecyclerView.Adapter<ScheduleInfoAdapter.ViewHolder>() {

    private val inflater:LayoutInflater = LayoutInflater.from(context)
    private var scheduleList = emptyList<ScheduleInfo>()
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var scheduleItem: CardView
        var startHour: TextView
        var gubun: TextView //" : "
        var startMinute: TextView
        var alarmOn:ImageView
        var scheduleExplain:TextView


        init {
            // Define click listener for the ViewHolder's View.
            scheduleItem = view.findViewById(R.id.scheduleItem)
            startHour = scheduleItem.findViewById(R.id.startHour)
            gubun = scheduleItem.findViewById(R.id.gubun)
            startMinute = scheduleItem.findViewById(R.id.startMinute)
            alarmOn = scheduleItem.findViewById(R.id.alarmOn)
            scheduleExplain = scheduleItem.findViewById(R.id.scheduleExplain)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = inflater.inflate(R.layout.item_schedule, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val schedule = scheduleList[position]
        viewHolder.startHour.text = schedule.scheduleStartHour.toString()
        viewHolder.gubun.text = " : "
        viewHolder.startMinute.text = schedule.scheduleStartMinute.toString()
        viewHolder.scheduleExplain.text = schedule.scheduleExplain
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = scheduleList.size

    @SuppressLint("NotifyDataSetChanged")
    internal fun setData(scheduleInfo: List<ScheduleInfo>){
        this.scheduleList = scheduleInfo
        notifyDataSetChanged()
    }

}
