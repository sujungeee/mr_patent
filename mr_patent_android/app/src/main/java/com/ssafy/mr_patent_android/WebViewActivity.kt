package com.ssafy.mr_patent_android

import android.os.Bundle
import android.view.TextureView
import android.webkit.WebView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true

        val btn = findViewById<TextView>(R.id.webView_title)
        btn.setOnClickListener {
            finish()
        }


        val url = intent.getStringExtra("url") ?: "https://www.patent.go.kr/smart/portal/Main.do"
        webView.loadUrl(url)
    }
}