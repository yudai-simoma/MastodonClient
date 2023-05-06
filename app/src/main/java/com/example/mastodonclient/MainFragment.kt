package com.example.mastodonclient

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.mastodonclient.databinding.FragmentMainBinding
import android.util.Log
import retrofit2.Retrofit

class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        //ログ出力用のタグ
        private val TAG = MainFragment::class.java.simpleName
        //アクセスするMastodonインスタンスのURL
        private const val API_BASE_URL = "https://androidbook2020.com"
    }

    //RetrofitでAPIにアクセスする準備。アクセス先のURLとAPIの定義を指定して初期化する
    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .build()
    private val api = retrofit.create(MastodonApi::class.java)

    private var binding: FragmentMainBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DataBindingUtil.bind(view)
        binding?.button?.setOnClickListener {
            binding?.button?.text = "clicked"
            //公開タイムラインAPIにアクセスして、サーバから応答を文字列で取得
            val response = api.fetchPublicTimeline()
                .execute().body()?.string()
            //取得した結果をログに出力
            Log.d(TAG, response!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }
}
