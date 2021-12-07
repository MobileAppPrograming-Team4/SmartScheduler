package com.example.smartscheduler.Activity

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smartscheduler.R

class CarNavigationActivity : AppCompatActivity() {
    lateinit var web: WebView
    lateinit var endnaviBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnavigation)

        var intent = intent
        var url = intent.getStringExtra("uri")

        web = findViewById(R.id.webview1)
        web.loadUrl(url.toString())

        endnaviBtn = findViewById(R.id.endnaviBtn)
        endnaviBtn.setOnClickListener {
            finish()
        }
    }
}