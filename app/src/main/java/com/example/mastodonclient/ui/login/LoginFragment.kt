package com.example.mastodonclient.ui.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
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

        //サーバーに指定するリダイレクトURIの定数。BuildConfig.APPLICATION_IDはアプリケーションIDを表す定数
        private const val REDIRECT_URI = "auth://${BuildConfig.APPLICATION_ID}"
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

    fun requestAccessToken(code: String) {
        //アクセストークンをリクエスト
        viewModel.requestAccessToken(
            BuildConfig.CLIENT_KEY,
            BuildConfig.CLIENT_SECRET,
            REDIRECT_URI,
            BuildConfig.CLIENT_SCOPES,
            code
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //表示したActivityがCallbackを実装しているか検査してコールバックを保持
        if (context is Callback) {
            callback = context
        }
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
            //サーバーに指定するリダイレクトURIを切り替え
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", BuildConfig.CLIENT_SCOPES)
            .build()

        //URI指定（暗黙的インテント）でブラウザを起動
        val intent = Intent(Intent.ACTION_VIEW, authUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        startActivity(intent)
    }
}

