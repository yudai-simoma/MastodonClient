package com.example.mastodonclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.databinding.ListItemTootBinding

class TootListAdapter(
    private val layoutInflater: LayoutInflater,
    private val tootList: ArrayList<Toot>,
    private val callback: Callback?
    //RecyclerView.Adapterを継承する
) : RecyclerView.Adapter<TootListAdapter.ViewHolder>() {

    interface Callback {
        fun openDetail(toot: Toot)
    }

    //リストの要素数を知らせる
    override fun getItemCount() = tootList.size

    //viewTypeに応じたViewHolderのインスタンスを生成する
    //今回はviewHolderは1つだけviewTypeは考慮しない
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = DataBindingUtil.inflate<ListItemTootBinding>(
            layoutInflater,
            R.layout.list_item_toot,
            parent,
            false
        )
        return ViewHolder(binding, callback)
    }

    //onCreateViewHolderで生成したViewHolderインスタンスに。リストのposition
    //で示される位置の要素をバインドする
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(tootList[position])
    }

    class ViewHolder(
        private val binding: ListItemTootBinding,
        private val callback: Callback?
    ) : RecyclerView.ViewHolder(binding.root) {
        //Tootオブジェクトの内容をDataBindingに表示する
        fun bind(toot: Toot) {
            binding.toot = toot
            //要素をタップしたイベントのリスナーを設定
            binding.root.setOnClickListener {
                callback?.openDetail(toot)
            }
        }
    }
}