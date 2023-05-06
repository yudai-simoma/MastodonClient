package com.example.mastodonclient

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.mastodonclient.databinding.FragmentTootListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.atomic.AtomicBoolean
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

    //読み込み中の状態を保持するメンバ変数
    private var isLoading = AtomicBoolean()
    //次の読み込みが必要か・必要でないかを保持するメンバ変数
    private var hasNext = AtomicBoolean().apply { set(true) }

    //RecyclerViewのスクロールイベントを受け取るリスナー
    private val loadNextScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            //すでに読み込み処理が実行されているか、次の読み込みの必要がなければ処理を抜ける
            if (isLoading.get() || !hasNext.get()) {
                return
            }

            //1番下の要素が見えていれば、追加で読み込みを実行
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if ((totalItemCount - visibleItemCount) <= firstVisibleItemPosition) {
                loadNext()
            }
        }
    }

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
            //RecyclerViewにリスナーを設定
            it.addOnScrollListener(loadNextScrollListener)
        }
        //Pull-to-Refresh操作時のイベントリスナーを設定
        bindingData.swipeRefreshLayout.setOnRefreshListener {
            tootList.clear()
            loadNext()
        }

        loadNext()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }

    //読み込み中表示のONを設定するメソッド。メインスレッド(Dispatchers)で実行する
    private suspend fun showProgress() = withContext(Dispatchers.Main) {
        binding?.swipeRefreshLayout?.isRefreshing = true
    }

    //読み込み中表示のOFFを設定するメソッド。メインスレッド(Dispatchers)で実行する
    private suspend fun dismissProgress() = withContext(Dispatchers.Main) {
        binding?.swipeRefreshLayout?.isRefreshing = false
    }

    private fun loadNext() {
        coroutineScope.launch {
            //状態を読み込み中の状態に設定
            isLoading.set(true)
            showProgress()

            val tootListResponse = api.fetchPublicTimeline(
                maxId = tootList.lastOrNull()?.id,
                onlyMedia = true
            )
            tootList.addAll(tootListResponse.filter { !it.sensitive })
            reloadTootList()

            //サーバーから取得したTodoリストに要素が空で無ければ次の読み込みが必要
            hasNext.set(tootListResponse.isNotEmpty())
            //読み込み中の状態を解除
            isLoading.set(false)
            dismissProgress()
        }
    }

    private suspend fun reloadTootList() = withContext(Dispatchers.Main) {
        //Adapterにデータが更新されたことを伝える
        adapter.notifyDataSetChanged()
    }

}