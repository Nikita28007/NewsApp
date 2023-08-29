package com.example.testapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class WebviewFragment : Fragment() {
    lateinit var web : WebView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.webview_fragment, container, false)
         web = view.findViewById<WebView>(R.id.webview)
        web.settings.javaScriptEnabled = true
        web.webViewClient= WebViewClient()
        val URL = arguments?.getString("URL")
        web.loadUrl(URL.toString())

        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback : OnBackPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (web.canGoBack()){
                    web.goBack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }
}