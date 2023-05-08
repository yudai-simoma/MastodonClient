package com.example.mastodonclient

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mastodonclient.databinding.FragmentTootListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class TootListFragment : Fragment(R.layout.fragment_toot_list) {

    companion object {
        val TAG = TootListFragment::class.java.simpleName

        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    private var binding: FragmentTootListBinding? = null

    // SSL 証明書を検証しない OkHttpClient の設定
    private val tootRepository = TootRepository(API_BASE_URL)

    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    //値の変更が可能(Mutable)なLiveDataを宣言
    private val isLoading = MutableLiveData<Boolean>()
    //次の読み込みが必要か・必要でないかを保持するメンバ変数
    private var hasNext = AtomicBoolean().apply { set(true) }

    //RecyclerViewのスクロールイベントを受け取るリスナー
    private val loadNextScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            //?: (エルビス演算子)でnullの場合は処理を終了(LiveDataの値はnullの可能性有)
            val isLoadingSnapshot = isLoading.value ?: return
            if (isLoadingSnapshot || !hasNext.get()) {
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

    //値の変更が可能なLiveDataを宣言
    private val tootList = MutableLiveData<ArrayList<Toot>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //LiveDataに値がない時は、空のリストをインスタンス化してLiveDataに設定
        val tootListSnapshot = tootList.value ?: ArrayList<Toot>().also {
            tootList.value = it
        }

        //TootListAdapterをインスタンス化する。コンストラクタにtootListを与える
        adapter = TootListAdapter(layoutInflater, tootListSnapshot)
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
            tootListSnapshot.clear()
            loadNext()
        }

        //LiveDataの値を監視する、変更はObserverで受け取る
        isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        tootList.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })

        loadNext()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }

    private fun loadNext() {
        //コルーチンをlifecycleScopeで実行
        lifecycleScope.launch {
            //LiveDataの値を変更する。
            isLoading.postValue(true)

            val tootListSnapshot = tootList.value ?: return@launch

            //ネットワーク接続処理はI/O用のスレッドを指定
            val tootListResponse = tootRepository.fetchPublicTimeline(
                maxId = tootListSnapshot.lastOrNull()?.id,
                onlyMedia = true
            )
            Log.d(TAG, "fetchPublicTimeline")

            tootListSnapshot.addAll(tootListResponse.filter { !it.sensitive })
            Log.d(TAG, "addAll")

            tootList.postValue(tootListSnapshot)

            //サーバーから取得したTodoリストに要素が空で無ければ次の読み込みが必要
            hasNext.set(tootListResponse.isNotEmpty())
            isLoading.postValue(false)
            Log.d(TAG, "dismissProgress")
        }
    }
}