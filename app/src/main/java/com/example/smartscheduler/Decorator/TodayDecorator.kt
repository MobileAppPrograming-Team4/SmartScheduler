package com.example.smartscheduler.Decorator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TodayDecorator(context: Context): DayViewDecorator {
    private var date = CalendarDay.today()
    //@SuppressLint("UseCompatLoadingForDrawables")
    //val drawable: Drawable = context.resources.getDrawable(R.drawable.ic_baseline_crop_din_24)
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }
    override fun decorate(view: DayViewFacade?) {
        //view?.setBackgroundDrawable(drawable)
        view?.addSpan(StyleSpan(Typeface.BOLD))
        view?.addSpan(RelativeSizeSpan(1.3f))
        view?.addSpan(ForegroundColorSpan(Color.MAGENTA))
    }
}