package com.example.mastodonclient

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.mastodonclient.databinding.FragmentMainBinding
import android.util.Log
import retrofit2.Retrofit
//本にはないが、SSL認証が切れているため無効にする設定に使用
import okhttp3.OkHttpClient
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainFragment : Fragment(R.layout.fragment_main) {
    companion object {
        //ログ出力用のタグ
        private val TAG = MainFragment::class.java.simpleName
        //アクセスするMastodonインスタンスのURL
        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // SSL 証明書を検証しない OkHttpClient の設定
    private val retrofit: Retrofit
    private val api: MastodonApi

    init {
        // SSL 証明書を検証しない OkHttpClient の設定
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        //RetrofitでAPIにアクセスする準備。アクセス先のURLとAPIの定義を指定して初期化する
        retrofit = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient) // ここでSSL認証無効の設定を適用
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        api = retrofit.create(MastodonApi::class.java)
    }

    private var binding: FragmentMainBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)
        binding?.button?.setOnClickListener {
            binding?.button?.text = "clicked"
            //IO用のスレッドで非同期処理を実行
            CoroutineScope(Dispatchers.IO).launch {
                val tootList = api.fetchPublicTimeline()
                showTootList(tootList)

//                //公開タイムラインAPIにアクセスして、サーバから応答を文字列で取得
//                val response = api.fetchPublicTimeline().string()
//                //取得した結果をログに出力
//                Log.d(TAG, response)
//                //メインスレッドで実行
//                withContext(Dispatchers.Main) {
//                    binding?.button?.text = response
//                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }

    private suspend fun showTootList(
        tootList: List<Toot>
    ) = withContext(Dispatchers.Main) {
        val binding = binding ?: return@withContext
        val accountNameList = tootList.map { it.account.displayName }
        binding.button.text = accountNameList.joinToString("\n")
    }
}
