package com.chevstrap.rbx.ui.views.settings.pages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import chevstrap.preference.PrefsManager
import com.chevstrap.rbx.App
import com.chevstrap.rbx.R
import com.chevstrap.rbx.SettingsActivity

class LoginWebFragment : Fragment() {
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_webview, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById<WebView>(R.id.webView).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url.toString()
                    if (url.endsWith("/app")) {
                        view.evaluateJavascript(RAW_JS_SNIPPET, null)
                        view.visibility = View.GONE
                        return true
                    }
                    return false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView,
                    url: String?,
                    message: String,
                    result: JsResult
                ): Boolean {
                    onTokenRetrieved(message)
                    view.visibility = View.GONE
                    result.confirm()
                    return true
                }
            }

            loadUrl(LOGIN_URL)
        }
    }

    private fun onTokenRetrieved(token: String) {
        val logIdentifier = "LoginWebFragment::onTokenRetrieved"
        try {
            PrefsManager.setToken(token)
        } catch (_: Exception) {
            App.logger.writeLine(logIdentifier, "Failed to encrypt token or set token")
        }

        (App.savedSettingsActivity as? SettingsActivity)?.let {
            it.movePage("Logged")

            App.logger.writeLine(logIdentifier, "Moving page to Logged")
        } ?: App.logger.writeLine(logIdentifier, "Settings activity not found")
    }

    override fun onDestroyView() {
        webView?.apply {
            stopLoading()
            webChromeClient = null
            webViewClient = WebViewClient()
            destroy()
        }
        webView = null
        super.onDestroyView()
    }

    companion object {
        private const val LOGIN_URL = "https://discord.com/login"
        private const val RAW_JS_SNIPPET =
            "(function(){var i=document.createElement('iframe');document.body.appendChild(i);alert(i.contentWindow.localStorage.token.slice(1,-1))})()"
    }
}