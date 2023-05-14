package com.example.mastodonclient.ui.toot_list

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.example.mastodonclient.BuildConfig
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.FragmentTootListBinding
import com.example.mastodonclient.entity.Account
import com.example.mastodonclient.entity.Toot
import com.example.mastodonclient.ui.toot_detail.TootDetailActivity

class TootListFragment : Fragment(R.layout.fragment_toot_list),
        TootListAdapter.Callback {
    companion object {
        val TAG = TootListFragment::class.java.simpleName

        //Bundleオブジェクトに値を出し入れする時に使うキーを定義
        private const val BUNDLE_KEY_TIMELINE_TYPE_ORDINAL = "timeline_type_ordinal"

        @JvmStatic
        fun newInstance(timelineType: TimelineType): TootListFragment {
            val args = Bundle().apply {
                //列挙型における順序・インデックスをInt型で入れる
                putInt(BUNDLE_KEY_TIMELINE_TYPE_ORDINAL, timelineType.ordinal)
            }
            return TootListFragment().apply {
                arguments = args
            }
        }
    }

    private var binding: FragmentTootListBinding? = null

    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var timelineType = TimelineType.PublicTimeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().also {
            //Bundleオブジェクトからタイムラインの種類を列挙型のordinalとして取り出す
            val typeOrdinal = it.getInt(
                BUNDLE_KEY_TIMELINE_TYPE_ORDINAL,
                TimelineType.PublicTimeline.ordinal
            )
            //列挙型の値をプロパティに設定
            timelineType = TimelineType.values()[typeOrdinal]
        }
    }


    //viewModelを生成
    private val viewModel: TootListViewModel by viewModels {
        TootListViewModelFactory(
            //APIURL
            BuildConfig.INSTANCE_URL,
            BuildConfig.USERNAME,
            timelineType,
            lifecycleScope,
            requireContext()
        )
    }

    //RecyclerViewのスクロールイベントを受け取るリスナー
    private val loadNextScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            //?: (エルビス演算子)でnullの場合は処理を終了(LiveDataの値はnullの可能性有)
            val isLoadingSnapshot = viewModel.isLoading.value ?: return
            if (isLoadingSnapshot || !viewModel.hasNext) {
                return
            }

            //1番下の要素が見えていれば、追加で読み込みを実行
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if ((totalItemCount - visibleItemCount) <= firstVisibleItemPosition) {
                viewModel.loadNext()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //LiveDataに値がない時は、空のリストをインスタンス化してLiveDataに設定
        val tootListSnapshot = viewModel.tootList.value ?: ArrayList<Toot>().also {
            viewModel.tootList.value = it
        }

        //TootListAdapterをインスタンス化する。コンストラクタにtootListを与える
        adapter = TootListAdapter(layoutInflater, tootListSnapshot, this)
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
            viewModel.clear()
            viewModel.loadNext()
        }

        //LiveDataの値を監視する、変更はObserverで受け取る
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        //ViewModel側の変更を受けてアカウント情報を表示
        viewModel.accountInfo.observe(viewLifecycleOwner, Observer {
            showAccountInfo(it)
        })
        viewModel.tootList.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })

        viewLifecycleOwner.lifecycle.addObserver(viewModel)
    }

    private fun showAccountInfo(accountInfo: Account) {
        val activity = requireActivity()
        if (activity is AppCompatActivity) {
            //ActionBarのサブタイトルにユーザー名を設定
            activity.supportActionBar?.subtitle = accountInfo.username
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }

    //要素をタップ時にTootListAdapter内から呼び出される
    override fun openDetail(toot: Toot) {
        val intent = TootDetailActivity.newIntent(requireContext(), toot)
        //Activityの起動
        startActivity(intent)
    }
}