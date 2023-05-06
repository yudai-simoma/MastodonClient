package com.example.mastodonclient

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.mastodonclient.databinding.FragmentTootListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
//本にはないが、SSL認証が切れているため無効にする設定に使用
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TootListFragment : Fragment(R.layout.fragment_toot_list) {

    companion object {
        val TAG = TootListFragment::class.java.simpleName

        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    private var binding: FragmentTootListBinding? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // SSL 証明書を検証しない OkHttpClient の設定
    private val retrofit: Retrofit
    private val api: MastodonApi

    init {
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
            //Moshiを使ってJSONをパースするようにRetrofitに登録（追加）
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        api = retrofit.create(MastodonApi::class.java)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    //読み込み済みのTodoのリストをクラスのメンバ変数で保持する
    private val tootList = ArrayList<Toot>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TootListAdapterをインスタンス化する。コンストラクタにtootListを与える
        adapter = TootListAdapter(layoutInflater, tootList)
        //表示するリストの並べ方（レイアウト方法）を指定する。
        //VERTICALは縦方向に並べる指定
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false)
        val bindingData: FragmentTootListBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        //TootListAdapter（表示内容）とLayoutManager（レイアウト方法）を
        //RecyclerViewに設定する
        bindingData.recyclerView.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }

        coroutineScope.launch {
            val tootListResponse = api.fetchPublicTimeline()
            //APIから取得したTootのリストをメンバ変数のリストに追加して表示内容を
            //再読み込み
            tootList.addAll(tootListResponse)
            reloadTootList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }

    private suspend fun reloadTootList() = withContext(Dispatchers.Main) {
        //Adapterにデータが更新されたことを伝える
        adapter.notifyDataSetChanged()
    }

}