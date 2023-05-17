package com.example.mastodonclient.ui.login

import android.content.Context
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
import androidx.lifecycle.Observer
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

    //認証完了をActiivtyに伝えるコールバック
    interface Callback {
        fun onAuthCompleted()
    }

    //コールバックを保持するプロパティ。nullの場合がある
    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //表示したActivityがCallbackを実装しているか検査してコールバックを保持
        if (context is Callback) {
            callback = context
        }
    }


    //codeを受け取るコールバック関数を宣言
    private val onObtainCode = fun(code: String) {
        viewModel.requestAccessToken(
            BuildConfig.CLIENT_KEY,
            BuildConfig.CLIENT_SECRET,
            BuildConfig.CLIENT_REDIRECT_URI,
            BuildConfig.CLIENT_SCOPES,
            code
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bindingData: FragmentLoginBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        //アクセストークンが保存されたらActivityにコールバックする
        viewModel.accessTokenSaved.observe(viewLifecycleOwner, Observer {
            callback?.onAuthCompleted()
        })

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
        bindingData.webview.webViewClient = InnerWebViewClient(onObtainCode)
        //JavaScriptを有効化
        bindingData.webview.settings.javaScriptEnabled = true
        //組み立てたURLを読み込む
        bindingData.webview.loadUrl(authUri.toString())
    }

    private class InnerWebViewClient(
        val onObtainCode: (code: String) -> Unit
    ) : WebViewClient() {
        //証明書を無視する関数
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed() // 証明書エラーを無視して継続する
        }
        //WebView内でページ遷移(URL変更)イベント
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view ?: return

            //クエリにcodeが含まれているかチェック。無ければ処理を抜ける
            val code = Uri.parse(view.url).getQueryParameter("code")
            code ?: return

            //codeをコールバック関数onObtainCodeの引数として実行
            onObtainCode(code)
        }

    }
}

