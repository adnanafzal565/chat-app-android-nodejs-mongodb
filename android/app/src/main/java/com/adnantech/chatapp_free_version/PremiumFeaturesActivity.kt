package com.adnantech.chatapp_free_version

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity


class PremiumFeaturesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_features)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        title = "Premium features"
        val browser: WebView = findViewById(R.id.webview)
        browser.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        browser.settings.builtInZoomControls = true
        browser.setInitialScale(1)
        browser.settings.loadWithOverviewMode = true
        browser.settings.useWideViewPort = true
        browser.settings.loadsImagesAutomatically = true
        browser.settings.javaScriptEnabled = true
//        browser.settings.setAppCacheEnabled(false)
//        browser.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        browser.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
//        browser.loadUrl("https://adnan-tech.com/android-chat-app-in-kotlin-and-node-js?v=" + Date().time)
        browser.loadUrl("https://adnan-tech.com/android-chat-app-in-kotlin-and-node-js?v=2")

//        renderYTVideo(findViewById(R.id.ytSearchVoiceNotes), "mOAEZTs5AlY")
//        renderYTVideo(findViewById(R.id.ytSearchImages), "aw-z7Dq7TuA")
//        renderYTVideo(findViewById(R.id.ytEncryption), "wfSR02tOiXo")
//        renderYTVideo(findViewById(R.id.ytVoiceNotes), "fVhWyxjJyDs")
//        renderYTVideo(findViewById(R.id.ytGroups), "HWf7QHTDDeM")
//        renderYTVideo(findViewById(R.id.ytStatus), "85lpDLP8bN8")
//        renderYTVideo(findViewById(R.id.ytBlueticks), "BaxAmGJTbnM")
//        renderYTVideo(findViewById(R.id.ytSearchMessages), "h_BpFkWZfvY")
    }

    fun renderYTVideo(webView: WebView, myVideoYoutubeId: String) {
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        })

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        webView.loadUrl("https://www.youtube.com/embed/$myVideoYoutubeId")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
