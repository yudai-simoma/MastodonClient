package com.example.mastodonclient.ui.toot_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.ListItemTootBinding
import com.example.mastodonclient.entity.Toot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TootListAdapter(
    private val layoutInflater: LayoutInflater,
    private val coroutineScope: CoroutineScope,
    private val callback: Callback?
    //RecyclerView.Adapterを継承する
) : RecyclerView.Adapter<TootListAdapter.ViewHolder>() {

    interface Callback {
        fun openDetail(toot: Toot)
        fun delete(toot: Toot)
    }

    //2つの差分を判定する条件を定めるクラス
    private class RecyclerDiffCallback(
        private val oldList: List<Toot>,
        private val newList: List<Toot>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        //要素が同じか判定
        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = oldList[oldItemPosition] == newList[newItemPosition]

        //要素の「内容」が同じか判定
        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    var tootList: ArrayList<Toot> = ArrayList()
        set(value) {
            coroutineScope.launch(Dispatchers.Main) {
                //差分計算を非同期で実行
                val diffResult = withContext(Dispatchers.Default) {
                    DiffUtil.calculateDiff(
                        RecyclerDiffCallback(field, value)
                    )
                }

                //プロパティに新しいリストを設定
                field = value

                //差分に基づく更新を実行
                diffResult.dispatchUpdatesTo(this@TootListAdapter)
            }
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
            binding.more.setOnClickListener {
                //PopupMenuをインスタンス化してメニューリソースlist_item_tootの内容を表示する
                PopupMenu(itemView.context, it).also { popupMenu ->
                    popupMenu.menuInflater.inflate(
                        R.menu.list_item_toot,
                        popupMenu.menu)
                    //ドロップダウンメニューから操作を選択したイベントのリスナーを設定
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when(menuItem.itemId) {
                            R.id.menu_delete -> callback?.delete(toot)
                        }
                        return@setOnMenuItemClickListener true
                    }
                }.show()
            }
        }
    }
}