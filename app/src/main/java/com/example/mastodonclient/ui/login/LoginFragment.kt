package com.example.mastodonclient.ui.login

import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mastodonclient.BuildConfig
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    companion object {
        val TAG = LoginFragment::class.java.simpleName
    }

    private var binding: FragmentLoginBinding? = null

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            BuildConfig.INSTANCE_URL,
            lifecycleScope,
            requireContext()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bindingData: FragmentLoginBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        //WebViewで読み込むURLを組み立てる
        val authUri = Uri.parse(BuildConfig.INSTANCE_URL)
            .buildUpon()
            .appendPath("oauth")
            .appendPath("authorize")
                //識別情報のクライアントキーを設定
            .appendQueryParameter("client_id", BuildConfig.CLIENT_KEY)
            .appendQueryParameter("redirect_uri", BuildConfig.CLIENT_REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", BuildConfig.CLIENT_SCOPES)
            .build()

        //WebViewの状態の変化を受け取るためのクラスを設定
        bindingData.webview.webViewClient = InnerWebViewClient()
        //JavaScriptを有効化
        bindingData.webview.settings.javaScriptEnabled = true
        //組み立てたURLを読み込む
        bindingData.webview.loadUrl(authUri.toString())

        bindingData.webview.webViewClient = InnerWebViewClient()
    }

    private class InnerWebViewClient() : WebViewClient() {
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed() // 証明書エラーを無視して継続する
        }
    }
}

