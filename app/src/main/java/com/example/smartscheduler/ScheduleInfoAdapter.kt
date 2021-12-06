package com.example.smartscheduler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscheduler.Database.ScheduleInfo


class ScheduleInfoAdapter(context: Context) :
    RecyclerView.Adapter<ScheduleInfoAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var scheduleList = emptyList<ScheduleInfo>()
    private var alarmOnColor =
        ContextCompat.getColor(context, R.color.yellow)
    private lateinit var scheduleClickListener: OnScheduleClickListener
    val context:Context = context
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var scheduleItem: CardView
        var start_finish_time: TextView
        var alarmOn: ImageView
        var alarm_hour_minute: TextView
        var scheduleExplain: TextView


        init {
            // Define click listener for the ViewHolder's View.
            scheduleItem = view.findViewById(R.id.scheduleItem)
            start_finish_time = scheduleItem.findViewById(R.id.start_finish_time)
            alarmOn = scheduleItem.findViewById(R.id.alarmOn)
            alarm_hour_minute = scheduleItem.findViewById(R.id.alarm_hour_minute)
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
        viewHolder.scheduleItem.setOnClickListener{ v ->
            showPopup(v, position)
        }
        val schedule = scheduleList[position]
        viewHolder.start_finish_time.text = "${schedule.scheduleStartHour}:${schedule.scheduleStartMinute} ~ ${schedule.scheduleFinishHour}:${schedule.scheduleFinishMinute}"
        if (schedule.setAlarm) {
            viewHolder.alarmOn.setColorFilter(alarmOnColor, PorterDuff.Mode.SRC_IN)
            viewHolder.alarm_hour_minute.text = "${schedule.alarmHour}:${schedule.alarmMinute}"
        } else {
            viewHolder.alarmOn.setColorFilter(null)
            viewHolder.alarm_hour_minute.text = "OFF"
        }
        viewHolder.scheduleExplain.text = schedule.scheduleExplain

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = scheduleList.size

    @SuppressLint("NotifyDataSetChanged")
    internal fun setData(scheduleInfo: List<ScheduleInfo>) {
        this.scheduleList = scheduleInfo
        notifyDataSetChanged()
    }

    private fun showPopup(v:View, position:Int){
        PopupMenu(context, v).apply{
            //MainActivity implements OnScheduleClickListener
            setOnMenuItemClickListener { item ->
                onMenuItemClick(item, position)
                true
            }
            inflate(R.menu.schedule_modify_delete)
            show()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun onMenuItemClick(item: MenuItem, position:Int): Boolean{
        return when(item.itemId){
            R.id.modify -> {
                this.scheduleClickListener.modify(position)
                true
            }
            R.id.route -> {
                this.scheduleClickListener.route(position)
                true
            }
            R.id.delete->{
                this.scheduleClickListener.delete(position)
                Toast.makeText(context, "일정을 삭제했습니다", Toast.LENGTH_LONG).show()
                notifyItemRemoved(position)
                true
            }
            R.id.carroute->{
                this.scheduleClickListener.carroute(position)
                true
            }
            else -> false
        }
    }
    
    fun setScheduleClickListener(onScheduleClickListener: OnScheduleClickListener){
        this.scheduleClickListener = onScheduleClickListener
    }
}
