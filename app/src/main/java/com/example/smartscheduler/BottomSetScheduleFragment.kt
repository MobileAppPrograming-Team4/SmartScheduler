package com.example.smartscheduler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class BottomSetScheduleFragment: BottomSheetDialogFragment(), View.OnClickListener {
    private var selectedHour:Int = 0
    private var selectedMinute:Int = 0
    private var startOrFinish:Int? = 0
    private lateinit var listener: CompleteListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_bottomsetschedule, container, false)
        val year_to_day_TextView = view.findViewById<TextView>(R.id.year_month_date_day_TextView)
        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)
        val cancel = view.findViewById<TextView>(R.id.cancel)
        val complete = view.findViewById<TextView>(R.id.complete)

        val year = arguments?.getInt("year")
        val month = arguments?.getInt("month")
        val date = arguments?.getInt("date")
//        selectedHour = arguments?.getInt("hour")!!
//        selectedMinute = arguments?.getInt("minute")!!
        startOrFinish = arguments?.getInt("startOrFinish",0)

        year_to_day_TextView.text = "${year}년 ${month}월 ${date}일"

        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener { timePicker, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
        }

        cancel.setOnClickListener {
            dismiss()
        }
        complete.setOnClickListener(this)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as CompleteListener
        setCompleteListener(listener)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog //둥근 모서리

    private fun setCompleteListener(listener: CompleteListener){
        // Assign the listener implementing events interface that will receive the events
        this.listener = listener
    }

    override fun onClick(p0: View?) {
        listener.setTime(selectedHour, selectedMinute, startOrFinish!!)
        dismiss()
    }

    interface CompleteListener{
        fun setTime(hour:Int, minute:Int, startOrFinish:Int)
    }

}